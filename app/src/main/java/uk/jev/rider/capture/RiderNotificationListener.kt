package uk.jev.rider.capture

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import uk.jev.rider.db.OfferDatabase
import uk.jev.rider.output.RecommendationNotifier
import uk.jev.rider.scoring.LocalOfferScorer

class RiderNotificationListener : NotificationListenerService() {
    private val scorer by lazy { LocalOfferScorer() }
    private val database by lazy { OfferDatabase(this) }
    private val notifier by lazy { RecommendationNotifier(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val offer = CourierNotificationParser.parse(sbn) ?: return
        val recommendation = scorer.score(offer)

        database.insert(offer, recommendation)

        getSharedPreferences("jev_rider", Context.MODE_PRIVATE)
            .edit()
            .putString(
                "last_event",
                buildString {
                    appendLine("${offer.platform}: ${recommendation.decision} (${recommendation.score}/100)")
                    appendLine("Package: ${offer.packageName}")
                    appendLine("Pay: ${offer.payGbp ?: "unknown"}")
                    appendLine("Distance: ${offer.distanceMiles ?: "unknown"} miles")
                    appendLine("Time: ${offer.estimatedMinutes ?: "unknown"} min")
                    appendLine("Title: ${offer.rawTitle ?: ""}")
                    appendLine("Text: ${offer.rawText ?: ""}")
                    append("Reasons: ${recommendation.reasons.joinToString("; ")}")
                }
            )
            .apply()

        notifier.show(offer, recommendation)
    }
}
