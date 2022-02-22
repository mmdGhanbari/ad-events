package ir.tapsell.adevents.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ClickEvent(
    @JsonProperty("requestId")
    override val requestId: String,
    @JsonProperty("clickTime")
    val clickTime: Long,
) : Event()