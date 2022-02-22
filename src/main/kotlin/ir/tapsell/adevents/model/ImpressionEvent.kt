package ir.tapsell.adevents.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ImpressionEvent(
    @JsonProperty("requestId")
    override val requestId: String,
    @JsonProperty("adId")
    val adId: String,
    @JsonProperty("adTitle")
    val adTitle: String,
    @JsonProperty("advertiserCost")
    val advertiserCost: Double,
    @JsonProperty("appId")
    val appId: String,
    @JsonProperty("appTitle")
    val appTitle: String,
    @JsonProperty("impressionTime")
    val impressionTime: Long,
) : Event()