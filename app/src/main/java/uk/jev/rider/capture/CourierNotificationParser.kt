package uk.jev.rider.capture

import android.app.Notification
import android.service.notification.StatusBarNotification
import uk.jev.rider.model.CourierPlatform
import uk.jev.rider.model.NormalisedOffer

object CourierNotificationParser {
    private val moneyRegex = Regex("""£\s?(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val milesRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:mi|mile|miles)\b""", RegexOption.IGNORE_CASE)
    private val kmRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:km|kilometre|kilometer|kilometres|kilometers)\b""", RegexOption.IGNORE_CASE)
    private val minutesRegex = Regex("""(\d{1,3})\s*(?:min|mins|minute|minutes)\b""", RegexOption.IGNORE_CASE)

    fun parse(sbn: StatusBarNotification): NormalisedOffer? {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
        ).distinct().joinToString(" | ").trim().ifBlank { null }

        return parseText(
            packageName = sbn.packageName,
            title = title,
            text = text,
            receivedAtMs = sbn.postTime
        )
    }

    /**
     * Pure parser entry point so real notification formats can be regression-tested
     * without constructing Android StatusBarNotification objects.
     *
     * Confirmed Uber reference format:
     * title: "Delivery • £7.32"
     * text:  "19 min (3.9 mi) away"
     */
    fun parseText(
        packageName: String,
        title: String?,
        text: String?,
        receivedAtMs: Long
    ): NormalisedOffer? {
        val platform = platformFor(packageName)
        if (platform == CourierPlatform.UNKNOWN) return null

        val combined = listOfNotNull(title, text).joinToString(" | ")

        val pay = moneyRegex.find(combined)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        val miles = milesRegex.find(combined)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            ?: kmRegex.find(combined)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.times(0.621371)
        val minutes = minutesRegex.find(combined)?.groupValues?.getOrNull(1)?.toIntOrNull()

        return NormalisedOffer(
            platform = platform,
            packageName = packageName,
            rawTitle = title,
            rawText = text,
            receivedAtMs = receivedAtMs,
            payGbp = pay,
            distanceMiles = miles,
            estimatedMinutes = minutes
        )
    }

    internal fun platformFor(packageName: String): CourierPlatform {
        val value = packageName.lowercase()
        return when {
            "deliveroo" in value -> CourierPlatform.DELIVEROO
            "uber" in value -> CourierPlatform.UBER_EATS
            "justeat" in value || "just_eat" in value || "just-eat" in value -> CourierPlatform.JUST_EAT
            else -> CourierPlatform.UNKNOWN
        }
    }
}
