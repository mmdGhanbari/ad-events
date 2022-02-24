package ir.tapsell.adevents.actor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import ir.tapsell.adevents.entity.makeAdEvent
import ir.tapsell.adevents.job.FakeEventGenerator
import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.pipeline.PersistEvent
import ir.tapsell.adevents.pipeline.ProcessedEvent
import ir.tapsell.adevents.pipeline.actor.PersistActor
import ir.tapsell.adevents.repository.AdEventRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class PersistActorTest {
    private val adEventRepository: AdEventRepository = mockk()
    private val persistActor = PersistActor(adEventRepository)

    @Test()
    fun `should save impression event and update click-time based on click events`() {
        val impEvent = FakeEventGenerator.createFakeImpressionEvent()
        val clickEvent = ClickEvent(impEvent.requestId, impEvent.impressionTime + 1000)

        val adEvent = impEvent.makeAdEvent(0)
        val adEventAfterUpdate = impEvent.makeAdEvent(clickEvent.clickTime)

        every { adEventRepository.save(any()) } answers { firstArg() }
        every { adEventRepository.findByIdOrNull(adEvent.requestId) } returns adEvent

        runBlocking {
            val events: List<ProcessedEvent> = listOf(
                ProcessedEvent(impEvent),
                ProcessedEvent(clickEvent),
            )
            val sinkChannel = actor<PersistEvent> {
                persistActor.run(channel)
            }

            events.forEach { sinkChannel.send(PersistEvent(it)) }

            sinkChannel.close()
        }

        verifySequence {
            adEventRepository.save(adEvent)
            adEventRepository.findByIdOrNull(adEvent.requestId)
            adEventRepository.save(adEventAfterUpdate)
        }
    }

    @Test()
    fun `should handle out-of-order events`() {
        val impEvent = FakeEventGenerator.createFakeImpressionEvent()
        val clickEvent = ClickEvent(impEvent.requestId, impEvent.impressionTime + 1000)

        val adEvent = impEvent.makeAdEvent(clickEvent.clickTime)

        every { adEventRepository.save(any()) } answers { firstArg() }
        every { adEventRepository.findByIdOrNull(adEvent.requestId) } returns null

        runBlocking {
            val events: List<ProcessedEvent> = listOf(
                ProcessedEvent(clickEvent),
                ProcessedEvent(impEvent),
            )
            val sinkChannel = actor<PersistEvent> {
                persistActor.run(channel)
            }

            events.forEach { sinkChannel.send(PersistEvent(it)) }

            sinkChannel.close()
        }

        verifySequence {
            adEventRepository.findByIdOrNull(adEvent.requestId)
            adEventRepository.save(adEvent)
        }
    }
}