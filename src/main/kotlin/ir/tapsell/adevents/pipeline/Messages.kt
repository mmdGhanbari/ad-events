package ir.tapsell.adevents.pipeline

import kotlinx.coroutines.CompletableDeferred

class RequestData(val response: CompletableDeferred<List<Int>>)

class PersistData(val data: Int)