package uk.jev.rider.output

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import uk.jev.rider.model.NormalisedOffer
import uk.jev.rider.model.OfferRecommendation

class RecommendationNotifier(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "jev_recommendations"
    }

    fun show(offer: NormalisedOffer, recommendation: OfferRecommendation) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "JEV recommendations",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val rate = recommendation.estimatedPoundsPerHour?.let { " • £%.2f/hr".format(it) } ?: ""
        val ppm = recommendation.estimatedPoundsPerMile?.let { " • £%.2f/mi".format(it) } ?: ""
        val body = recommendation.reasons.take(2).joinToString(". ")
            .ifBlank { offer.rawText ?: "Courier offer captured" }

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${recommendation.decision.name} • ${recommendation.score}/100${rate}${ppm}")
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        manager.notify((offer.receivedAtMs and 0x7fffffff).toInt(), notification)
    }
}
