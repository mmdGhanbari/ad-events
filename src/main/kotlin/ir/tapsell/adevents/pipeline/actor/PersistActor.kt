package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class PersistActor {
    suspend fun run(channel: ReceiveChannel<PersistData>) {
        for (msg in channel) {
            delay(100)
//            println(msg.data)
        }
    }
}