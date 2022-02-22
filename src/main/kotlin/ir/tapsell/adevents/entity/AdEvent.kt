package ir.tapsell.adevents.entity

data class AdEvent(
    val requestId: String,
    val adId: String,
    val adTitle: String,
    val advertiserCost: Double,
    val appId: String,
    val appTitle: String,
    val impressionTime: Long,
    val clickTime: Long,
)