package offarkdev.pictureshower.analytics

open class AnalyticsEvent(
    val name: String,
    val eventProperties: Map<String, Any>,
    val providers: List<String> = listOf(FIREBASE_SENDER, APPSFLYER_SENDER),
)

class AppOpenedEvent(properties: Map<String, Any> = emptyMap()) :
    AnalyticsEvent(OPENED_APP, properties)

class PictureUpdateEvent : AnalyticsEvent(PICTURE_UPDATE, emptyMap())
class SharePictureEvent : AnalyticsEvent(SHARE_PICTURE, emptyMap())

const val OPENED_APP = "app_opened"
const val PICTURE_UPDATE = "picture_update"
const val SHARE_PICTURE = "share_picture"