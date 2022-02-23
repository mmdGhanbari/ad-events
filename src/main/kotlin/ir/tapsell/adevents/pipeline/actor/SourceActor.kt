package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class SourceActor {
    suspend fun run(channel: ReceiveChannel<SourceMessage>) {
        val buffer: MutableList<Event> = mutableListOf()

        for (msg in channel) {
            when (msg) {
                is RequestEvents -> {
                    val bucketSize = min(msg.bucketSize, buffer.size)
                    val events = buffer.slice(0 until bucketSize)
                    msg.response.complete(events)

                    buffer.subList(0, bucketSize).clear()
                }
                is BufferEvent -> buffer.add(msg.event)
            }

        }
    }
}