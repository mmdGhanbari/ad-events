package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.model.ImpressionEvent
import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class TransformerActor(
    @Value("\${actor.transformer.bucket_size}")
    val bucketSize: Int
) {
    suspend fun SendChannel<SourceMessage>.requestData(): List<Event> {
        val response = CompletableDeferred<List<Event>>()
        send(RequestEvents(response, bucketSize))
        return response.await()
    }

    suspend fun SendChannel<PersistEvent>.persistData(event: ProcessedEvent) {
        send(PersistEvent(event))
    }

    private fun List<Event>.process(): List<ProcessedEvent> {
        val timeMap: MutableMap<String, Long> = mutableMapOf()
        val visited: MutableMap<String, Boolean> = mutableMapOf()

        return this
            .filter {
                when (it) {
                    is ImpressionEvent -> {
                        visited[it.requestId] = true
                        true
                    }
                    is ClickEvent -> {
                        if (it.requestId in visited) {
                            timeMap[it.requestId] = max(
                                timeMap[it.requestId] ?: 0,
                                it.clickTime
                            )
                            false
                        } else true
                    }
                }
            }
            .map {
                when (it) {
                    is ImpressionEvent -> ProcessedEvent(it, timeMap[it.requestId])
                    is ClickEvent -> ProcessedEvent(it)
                }
            }
    }

    suspend fun run(
        sourceActor: SendChannel<SourceMessage>,
        persistActor: SendChannel<PersistEvent>,
    ) {
        while (true) {
            val receivedEvents = sourceActor.requestData()
            if (receivedEvents.isNotEmpty()) {
                val processedEvents = receivedEvents.process()
                processedEvents.forEach { persistActor.persistData(it) }
            } else delay(200)
        }
    }
}