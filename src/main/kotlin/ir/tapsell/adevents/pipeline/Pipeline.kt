package ir.tapsell.adevents.pipeline

import ir.tapsell.adevents.pipeline.actor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class Pipeline(
    private val sourceActor: SourceActor,
    private val transformerActor: TransformerActor,
    private val persistActor: PersistActor,
) {
    private var sourceChannel: SendChannel<SourceMessage>? = null

    fun getSourceActor() = sourceChannel

    private fun <E> CoroutineScope.spawnActor(
        fn: suspend (ReceiveChannel<E>) -> Unit
    ): SendChannel<E> = actor {
        fn(channel)
    }

    private fun CoroutineScope.spawnTransformers(
        sourceChannel: SendChannel<SourceMessage>,
        sinkChannel: SendChannel<PersistData>
    ) = repeat(3) {
        launch {
            transformerActor.run(sourceChannel, sinkChannel)
        }
    }

    fun run() = runBlocking(Dispatchers.Default) {
        // source
        val source: SendChannel<SourceMessage> = spawnActor { sourceActor.run(it) }
        sourceChannel = source

        // sink
        val sinkChannel: SendChannel<PersistData> =
            spawnActor { persistActor.run(it) }

        // transformers
        spawnTransformers(source, sinkChannel)
    }
}