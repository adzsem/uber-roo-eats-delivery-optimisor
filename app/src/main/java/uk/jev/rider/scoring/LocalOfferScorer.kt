package uk.jev.rider.scoring

import uk.jev.rider.model.BikeTelemetry
import uk.jev.rider.model.NormalisedOffer
import uk.jev.rider.model.OfferRecommendation
import uk.jev.rider.model.RecommendationDecision
import kotlin.math.roundToInt

class LocalOfferScorer(
    private val minimumPoundsPerMile: Double = 1.20,
    private val preferredPoundsPerMile: Double = 1.80,
    private val minimumPoundsPerHour: Double = 12.00,
    private val preferredPoundsPerHour: Double = 18.00,
    private val reserveRangeMiles: Double = 4.0
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
            estimatedPoundsPerMile = ppm?.let { (it * 100).roundToInt() / 100.0 },
            estimatedPoundsPerHour = pph?.let { (it * 100).roundToInt() / 100.0 },
            reasons = reasons
        )
    }
}
