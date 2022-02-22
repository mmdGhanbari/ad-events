package ir.tapsell.adevents.pipeline

import ir.tapsell.adevents.pipeline.actor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
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
    private fun <E> CoroutineScope.spawnActor(
        receiveChannel: ReceiveChannel<E>? = null,
        fn: suspend (ReceiveChannel<E>) -> Unit
    ): SendChannel<E> = actor {
        fn(receiveChannel ?: channel)
    }

    private fun CoroutineScope.spawnSources(
        sourceChannel: ReceiveChannel<RequestData>,
    ) = repeat(2) {
        spawnActor(sourceChannel) { sourceActor.run(it) }
    }

    private fun CoroutineScope.spawnTransformers(
        sourceChannel: SendChannel<RequestData>,
        sinkChannel: SendChannel<PersistData>
    ) = repeat(3) {
        launch {
            transformerActor.run(sourceChannel, sinkChannel)
        }
    }

    fun run() = runBlocking(Dispatchers.Default) {
        val sourceChannel = Channel<RequestData>()
        // source1
        spawnActor(sourceChannel) { sourceActor.run(it) }
        // source2
        spawnActor(sourceChannel) { sourceActor.run(it) }

        // sink
        val sinkChannel: SendChannel<PersistData> =
            spawnActor { persistActor.run(it) }

        // transformers
        spawnTransformers(sourceChannel, sinkChannel)
    }
}