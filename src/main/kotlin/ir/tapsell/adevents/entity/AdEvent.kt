package ir.tapsell.adevents.entity

import ir.tapsell.adevents.model.ImpressionEvent
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table("ad_event")
data class AdEvent(
    @PrimaryKey("request_id")
    val requestId: String,
    @field:Column("ad_id")
    val adId: String,
    @field:Column("ad_title")
    val adTitle: String,
    @field:Column("advertiser_cost")
    val advertiserCost: Double,
    @field:Column("app_id")
    val appId: String,
    @field:Column("app_title")
    val appTitle: String,
    @field:Column("impression_time")
    val impressionTime: Long,
    @field:Column("click_time")
    var clickTime: Long,
)

fun ImpressionEvent.makeAdEvent(clickTime: Long) = AdEvent(
    requestId = requestId,
    adId = adId,
    adTitle = adTitle,
    advertiserCost = advertiserCost,
    appId = appId,
    appTitle = appTitle,
    impressionTime = impressionTime,
    clickTime = clickTime,
)
