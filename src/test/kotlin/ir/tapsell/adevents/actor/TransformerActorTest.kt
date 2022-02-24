package ir.tapsell.adevents.actor

import ir.tapsell.adevents.job.FakeEventGenerator
import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.PersistEvent
import ir.tapsell.adevents.pipeline.ProcessedEvent
import ir.tapsell.adevents.pipeline.RequestEvents
import ir.tapsell.adevents.pipeline.SourceMessage
import ir.tapsell.adevents.pipeline.actor.TransformerActor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class TransformerActorTest {
    private val transformerActor = TransformerActor(10)

    private fun processEvents(
        inputEvents: List<Event>,
        waitUntil: Int,
    ): List<ProcessedEvent> {
        val processed: MutableList<ProcessedEvent> = mutableListOf()
        runBlocking {
            val sourceChannel = Channel<SourceMessage>()
            val sinkChannel = Channel<PersistEvent>()

            val job = launch { transformerActor.run(sourceChannel, sinkChannel) }

            with (sourceChannel.receive()) {
                if (this is RequestEvents)
                    response.complete(inputEvents)
            }

            for (msg in sinkChannel) {
                processed.add(msg.event)
                if (processed.size == waitUntil) break
            }

            job.cancel()
            sourceChannel.close()
            sinkChannel.close()
        }
        return processed
    }

    @Test
    fun `should process in-order events`() {
        val impEvent = FakeEventGenerator.createFakeImpressionEvent()
        val clickEvents: Array<ClickEvent> = Array(5) {
            ClickEvent(impEvent.requestId, impEvent.impressionTime + (it * 500))
        }

        val impEvent2 = FakeEventGenerator.createFakeImpressionEvent()
        val clickEventsSecondBatch: Array<ClickEvent> = Array(2) {
            ClickEvent(impEvent2.requestId, impEvent2.impressionTime + (it * 500))
        }

        val events = listOf(impEvent, impEvent2, *clickEvents, *clickEventsSecondBatch)
        val expected = listOf(
            ProcessedEvent(impEvent, impEvent.impressionTime + 2000),
            ProcessedEvent(impEvent2, impEvent2.impressionTime + 500),
        )

        val processed = processEvents(events, expected.size)
        assertEquals(processed, expected)
    }

    @Test
    fun `should process events including click events with no matching impressions`() {
        val impEvent = FakeEventGenerator.createFakeImpressionEvent()
        val clickEvent1 = ClickEvent(impEvent.requestId, impEvent.impressionTime + 500)

        val impEvent2 = FakeEventGenerator.createFakeImpressionEvent()
        val clickEventsSecondBatch: Array<ClickEvent> = Array(2) {
            ClickEvent(impEvent2.requestId, impEvent2.impressionTime + (it * 500))
        }

        val impEvent3 = FakeEventGenerator.createFakeImpressionEvent()
        val lastClickEvent = ClickEvent(impEvent3.requestId, impEvent3.impressionTime + 500)

        val events = listOf(clickEvent1, impEvent2, *clickEventsSecondBatch, lastClickEvent)
        val expected = listOf(
            ProcessedEvent(clickEvent1),
            ProcessedEvent(impEvent2, impEvent2.impressionTime + 500),
            ProcessedEvent(lastClickEvent),
            )

        val processed = processEvents(events, expected.size)
        assertEquals(processed, expected)
    }
}