package ir.tapsell.adevents.pipeline

import ir.tapsell.adevents.model.Event
import kotlinx.coroutines.CompletableDeferred

sealed class SourceMessage

class RequestEvents(
    val response: CompletableDeferred<List<Event>>,
    val bucketSize: Int,
) : SourceMessage()

class BufferEvent(val event: Event) : SourceMessage()


data class ProcessedEvent(
    val event: Event,
    val clickTime: Long? = null,
)

class PersistEvent(val event: ProcessedEvent)