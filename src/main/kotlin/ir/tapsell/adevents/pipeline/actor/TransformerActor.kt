package ir.tapsell.adevents.pipeline.actor

import ir.tapsell.adevents.pipeline.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.springframework.stereotype.Component

@Component
class TransformerActor {
    suspend fun SendChannel<RequestData>.requestData(): List<Int> {
        val response = CompletableDeferred<List<Int>>()
        send(RequestData(response))
        return response.await()
    }

    suspend fun SendChannel<PersistData>.persistData(data: Int) {
        send(PersistData(data))
    }

    suspend fun run(
        sourceActor: SendChannel<RequestData>,
        persistActor: SendChannel<PersistData>,
    ) {
        while (true) {
            val received = sourceActor.requestData()
            val transformed = received.sum()
            persistActor.persistData(transformed)
        }
    }
}