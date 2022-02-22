package ir.tapsell.adevents.service

import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.BufferEvent
import ir.tapsell.adevents.pipeline.Pipeline
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class KafkaService(
    @Value("\${kafka.topics.event}") val topic: String,
    @Autowired
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val pipeline: Pipeline,
) {
    fun sendEvents(event: Event) {
        val message: Message<Event> = MessageBuilder
            .withPayload(event)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .build()
        kafkaTemplate.send(message)
    }

    @KafkaListener(topics = ["\${kafka.topics.event}"], groupId = "event_group")
    fun consumeEvents(consumerRecord: ConsumerRecord<String, Event>, ack: Acknowledgment) {
        runBlocking { pipeline.getSourceActor()?.send(BufferEvent(consumerRecord.value())) }
        ack.acknowledge()
    }
}