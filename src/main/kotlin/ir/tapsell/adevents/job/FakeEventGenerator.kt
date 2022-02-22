package ir.tapsell.adevents.job

import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.model.ImpressionEvent
import ir.tapsell.adevents.service.KafkaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Component
class FakeEventGenerator(
    @Value("\${event_generator.workers.size}")
    val workerSize: Int,
    val kafkaService: KafkaService,
) {
    val eventBuffer: MutableList<Event> = emptyList<Event>().toMutableList()

    val adTitles = listOf("Beautiful Ad!", "Very Very Boring Ad!", "Black Screen!!")
    val appTitles = listOf("My Casual Game!", "My Serious Game!", "My Boring App")

    private fun createFakeImpressionEvent() = ImpressionEvent(
        requestId = UUID.randomUUID().toString(),
        adId = UUID.randomUUID().toString(),
        adTitle = adTitles.random(),
        advertiserCost = Random.nextDouble(200.0, 500.0),
        appId = UUID.randomUUID().toString(),
        appTitle = appTitles.random(),
        impressionTime = System.currentTimeMillis(),
    )

    private fun CoroutineScope.launchGeneratorWorker() = launch {
        while (true) {
            delay(Random.nextLong(2000))
            val impEvent = createFakeImpressionEvent()
            eventBuffer.add(impEvent)

            repeat(Random.nextInt(1, 4)) {
                delay(Random.nextLong(3000))
                val clickEvent = ClickEvent(impEvent.requestId, System.currentTimeMillis())
                eventBuffer.add(clickEvent)
            }
        }
    }

    private fun CoroutineScope.launchSender() = launch {
        while (true) {
            delay(1000)
            eventBuffer.forEach { kafkaService.sendEvents(it) }
            eventBuffer.clear()
        }
    }

    @Async
    fun run() = runBlocking {
        repeat(workerSize) {
            launchGeneratorWorker()
        }
        launchSender()
    }
}