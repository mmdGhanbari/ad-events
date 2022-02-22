package ir.tapsell.adevents.pipeline

import ir.tapsell.adevents.model.Event
import kotlinx.coroutines.CompletableDeferred

sealed class SourceMessage
class RequestEvents(val response: CompletableDeferred<List<Event>>) : SourceMessage()
class BufferEvent(val event: Event) : SourceMessage()

class PersistData(val data: Int)