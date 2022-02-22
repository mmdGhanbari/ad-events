package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.model.Event
import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.springframework.stereotype.Component

@Component
class TransformerActor {
    suspend fun SendChannel<SourceMessage>.requestData(): List<Event> {
        val response = CompletableDeferred<List<Event>>()
        send(RequestEvents(response))
        return response.await()
    }

    suspend fun SendChannel<PersistData>.persistData(data: Int) {
        send(PersistData(data))
    }

    suspend fun run(
        sourceActor: SendChannel<SourceMessage>,
        persistActor: SendChannel<PersistData>,
    ) {
        while (true) {
            val received = sourceActor.requestData()
//            val transformed = received
            persistActor.persistData(100)
        }
    }
}