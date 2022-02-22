package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.springframework.stereotype.Component

@Component
class SourceActor {
    suspend fun run(channel: ReceiveChannel<SourceMessage>) {
        val buffer: MutableList<Event> = mutableListOf()

        for (msg in channel) {
            when (msg) {
                is RequestEvents -> {
                    val events = buffer.toList()
                    msg.response.complete(events)
                    buffer.clear()
                }
                is BufferEvent -> buffer.add(msg.event)
            }

        }
    }
}