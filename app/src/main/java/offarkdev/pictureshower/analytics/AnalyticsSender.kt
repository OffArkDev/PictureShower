package offarkdev.pictureshower.analytics

import android.content.Context
import timber.log.Timber

abstract class AnalyticsSender(val name: String) {
    abstract fun sendEvent(eventName: String, eventProperties: Map<String, Any>)
}

class FirebaseAnalyticsSender(val context: Context) : AnalyticsSender(FIREBASE_SENDER) {
    override fun sendEvent(eventName: String, eventProperties: Map<String, Any>) {
       Timber.i("send event $eventName to Firebased")
    }
}

class AppsflyerAnalyticsSender : AnalyticsSender(APPSFLYER_SENDER) {
    override fun sendEvent(eventName: String, eventProperties: Map<String, Any>) {
        Timber.i("send event $eventName to Appsflyer")
    }
}

const val FIREBASE_SENDER = "firebase"
const val APPSFLYER_SENDER = "appsflyer"