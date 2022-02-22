package ir.tapsell.adevents

import ir.tapsell.adevents.job.FakeEventGenerator
import ir.tapsell.adevents.pipeline.Pipeline
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableAsync
class AdEventsApplication

@Component
class ApplicationReadyListener(
    private val pipeline: Pipeline,
    private val eventGenerator: FakeEventGenerator,
    ) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        eventGenerator.run()
        pipeline.run()
    }
}

fun main(args: Array<String>) {
    runApplication<AdEventsApplication>(*args)
}
