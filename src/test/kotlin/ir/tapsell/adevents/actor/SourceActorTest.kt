package ir.tapsell.adevents.actor

import ir.tapsell.adevents.job.FakeEventGenerator
import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.BufferEvent
import ir.tapsell.adevents.pipeline.RequestEvents
import ir.tapsell.adevents.pipeline.SourceMessage
import ir.tapsell.adevents.pipeline.actor.SourceActor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SourceActorTest {
    private val sourceActor = SourceActor()

    @Test
    fun `after buffering fake events, requesting them should return buffered events`() {
        runBlocking {
            // Spawn source actor
            val source: SendChannel<SourceMessage> = actor {
                sourceActor.run(channel)
            }

            // Generate fake events and send them to actor
            val impEvent = FakeEventGenerator.createFakeImpressionEvent()
            val clickEvents: Array<ClickEvent> = Array(5) {
                ClickEvent(impEvent.requestId, impEvent.impressionTime + (it * 500))
            }
            val events = listOf(impEvent, *clickEvents)
            events.forEach {
                source.send(BufferEvent(it))
            }

            // Request data and verify the result
            val response = CompletableDeferred<List<Event>>()
            source.send(RequestEvents(response, 10))

            assertEquals(response.await(), events)
            source.close()
        }
    }
}