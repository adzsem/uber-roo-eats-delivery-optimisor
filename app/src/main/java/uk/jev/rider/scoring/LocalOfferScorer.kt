package uk.jev.rider.scoring

import uk.jev.rider.config.RiderConfig
import uk.jev.rider.model.BikeTelemetry
import uk.jev.rider.model.NormalisedOffer
import uk.jev.rider.model.OfferRecommendation
import uk.jev.rider.model.RecommendationDecision
import kotlin.math.roundToInt

class LocalOfferScorer(
    private val minimumPoundsPerMile: Double = RiderConfig.MINIMUM_POUNDS_PER_MILE,
    private val preferredPoundsPerMile: Double = RiderConfig.PREFERRED_POUNDS_PER_MILE,
    private val minimumPoundsPerHour: Double = RiderConfig.MINIMUM_POUNDS_PER_HOUR,
    private val preferredPoundsPerHour: Double = RiderConfig.PREFERRED_POUNDS_PER_HOUR,
    private val reserveRangeMiles: Double = RiderConfig.RESERVE_RANGE_MILES,
    private val bikeMaxSpeedMph: Double = RiderConfig.BIKE_MAX_SPEED_MPH
) {
    fun score(
        offer: NormalisedOffer,
        bike: BikeTelemetry = BikeTelemetry()
    ): OfferRecommendation {
        val reasons = mutableListOf<String>()
        var score = 50

        val ppm = if (offer.payGbp != null && offer.distanceMiles != null && offer.distanceMiles > 0.0) {
            offer.payGbp / offer.distanceMiles
        } else null

        val pph = if (offer.payGbp != null && offer.estimatedMinutes != null && offer.estimatedMinutes > 0) {
            offer.payGbp * 60.0 / offer.estimatedMinutes
        } else null

        val requiredAverageSpeedMph =
            if (offer.distanceMiles != null && offer.estimatedMinutes != null && offer.estimatedMinutes > 0) {
                offer.distanceMiles / (offer.estimatedMinutes / 60.0)
            } else null

        val theoreticalMinimumRideMinutes =
            offer.distanceMiles?.takeIf { it > 0.0 }?.let { distance ->
                distance / bikeMaxSpeedMph * 60.0
            }

        if (ppm != null) {
            when {
                ppm >= preferredPoundsPerMile -> {
                    score += 20
                    reasons += "Strong £/mile"
                }
                ppm >= minimumPoundsPerMile -> {
                    score += 5
                    reasons += "Acceptable £/mile"
                }
                else -> {
                    score -= 25
                    reasons += "Low £/mile"
                }
            }
        } else {
            reasons += "Distance or pay missing from notification"
        }

        if (pph != null) {
            when {
                pph >= preferredPoundsPerHour -> {
                    score += 20
                    reasons += "Strong estimated £/hour"
                }
                pph >= minimumPoundsPerHour -> {
                    score += 5
                    reasons += "Acceptable estimated £/hour"
                }
                else -> {
                    score -= 25
                    reasons += "Low estimated £/hour"
                }
            }
        } else {
            reasons += "Time or pay missing from notification"
        }

        if (requiredAverageSpeedMph != null) {
            if (requiredAverageSpeedMph > bikeMaxSpeedMph) {
                score -= 20
                reasons += "Displayed estimate requires more than 19 mph average"
            } else {
                reasons += "Displayed estimate is feasible within 19 mph bike cap"
            }
        }

        if (bike.available) {
            val range = bike.estimatedRangeMiles
            val distance = offer.distanceMiles
            if (range != null && distance != null) {
                if (range - distance < reserveRangeMiles) {
                    score -= 30
                    reasons += "Bike range would fall below reserve"
                } else {
                    score += 5
                    reasons += "Bike range margin is healthy"
                }
            }

            val hottest = listOfNotNull(bike.controllerTempC, bike.motorTempC).maxOrNull()
            if (hottest != null && hottest >= 85.0) {
                score -= 25
                reasons += "High bike temperature"
            }
        }

        score = score.coerceIn(0, 100)

        val decision = when {
            ppm == null && pph == null -> RecommendationDecision.REVIEW
            score >= 65 -> RecommendationDecision.ACCEPT
            score <= 40 -> RecommendationDecision.DECLINE
            else -> RecommendationDecision.REVIEW
        }

        return OfferRecommendation(
            decision = decision,
            score = score,
            estimatedPoundsPerMile = ppm.round2(),
            estimatedPoundsPerHour = pph.round2(),
            requiredAverageSpeedMph = requiredAverageSpeedMph.round2(),
            theoreticalMinimumRideMinutes = theoreticalMinimumRideMinutes.round2(),
            reasons = reasons
        )
    }

    private fun Double?.round2(): Double? =
        this?.let { (it * 100).roundToInt() / 100.0 }
}
