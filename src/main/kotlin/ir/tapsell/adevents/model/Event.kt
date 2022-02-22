package ir.tapsell.adevents.model

sealed class Event {
    abstract val requestId: String
}