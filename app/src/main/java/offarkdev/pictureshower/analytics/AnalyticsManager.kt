package offarkdev.pictureshower.analytics

class AnalyticsManager(private val senders: List<AnalyticsSender>) {

    fun sendEvent(event: AnalyticsEvent) {
        senders.forEach {
            if (event.providers.contains(it.name))
                it.sendEvent(event.name, event.eventProperties)
        }
    }
}


