package uk.jev.rider.model

enum class CourierPlatform {
    DELIVEROO,
    UBER_EATS,
    JUST_EAT,
    UNKNOWN
}

data class NormalisedOffer(
    val platform: CourierPlatform,
    val packageName: String,
    val rawTitle: String?,
    val rawText: String?,
    val receivedAtMs: Long,
    val payGbp: Double? = null,
    val distanceMiles: Double? = null,
    val estimatedMinutes: Int? = null,
    val pickupLabel: String? = null,
    val dropoffLabel: String? = null
)

data class BikeTelemetry(
    val available: Boolean = false,
    val batteryVoltage: Double? = null,
    val batteryCurrent: Double? = null,
    val controllerTempC: Double? = null,
    val motorTempC: Double? = null,
    val speedMph: Double? = null,
    val whPerMile: Double? = null,
    val estimatedRangeMiles: Double? = null,
    val faultCode: String? = null
)

enum class RecommendationDecision {
    ACCEPT,
    DECLINE,
    REVIEW
}

data class OfferRecommendation(
    val decision: RecommendationDecision,
    val score: Int,
    val estimatedPoundsPerMile: Double?,
    val estimatedPoundsPerHour: Double?,
    val reasons: List<String>
)
