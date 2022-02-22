package ir.tapsell.adevents.utils.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import ir.tapsell.adevents.model.ClickEvent
import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.model.ImpressionEvent
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import kotlin.text.Charsets.UTF_8

class EventSerializer : Serializer<Event> {
    private val objectMapper = ObjectMapper()

    override fun serialize(topic: String?, data: Event?): ByteArray? {
        return objectMapper.writeValueAsBytes(data)
    }

    override fun close() {}
}

class EventDeserializer : Deserializer<Event> {
    private val objectMapper = ObjectMapper()

    override fun deserialize(topic: String?, data: ByteArray): Event? {
        val rootNode = objectMapper.readTree(String(data, UTF_8))
        return objectMapper.treeToValue(
            rootNode,
            if (rootNode.get("clickTime") == null)
                ImpressionEvent::class.java
            else ClickEvent::class.java,
        )
    }

    override fun close() {}
}
