package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class SourceActor {
    suspend fun run(channel: ReceiveChannel<RequestData>) {
        for (msg in channel) {
            delay(500)
            msg.response.complete(List(100) { it * 1 })
        }
    }
}