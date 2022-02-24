package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.entity.makeAdEvent
import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.ImpressionEvent
import ir.tapsell.adevents.pipeline.PersistEvent
import ir.tapsell.adevents.repository.AdEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class PersistActor(private val adEventRepository: AdEventRepository) {
    suspend fun run(channel: ReceiveChannel<PersistEvent>) {
        val waitingClickEvents: MutableMap<String, Long> = mutableMapOf()

        for (msg in channel) {
            withContext(Dispatchers.IO) {
                val processedEvent = msg.event
                processedEvent.event.apply {
                    when (this) {
                        is ClickEvent -> {
                            val adEvent = adEventRepository.findByIdOrNull(requestId)

                            if (adEvent == null) {
                                waitingClickEvents[requestId] = max(
                                    waitingClickEvents[requestId] ?: 0,
                                    clickTime,
                                )
                            } else if (clickTime > adEvent.clickTime) {
                                val newAdEvent = adEvent.copy(clickTime = clickTime)
                                adEventRepository.save(newAdEvent)
                            }
                        }
                        is ImpressionEvent -> {
                            val adEvent = makeAdEvent(
                                max(waitingClickEvents[requestId] ?: 0, processedEvent.clickTime ?: 0)
                            )
                            if (waitingClickEvents[requestId] != null)
                                waitingClickEvents.remove(requestId)

                            adEventRepository.save(adEvent)
                        }
                    }
                }
            }
        }
    }
}