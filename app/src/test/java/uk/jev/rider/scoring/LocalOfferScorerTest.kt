package uk.jev.rider.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.jev.rider.model.CourierPlatform
import uk.jev.rider.model.NormalisedOffer
import uk.jev.rider.model.RecommendationDecision

class LocalOfferScorerTest {
    @Test
    fun scoresReferenceUberOfferUsing19MphBikeCap() {
        val offer = NormalisedOffer(
            platform = CourierPlatform.UBER_EATS,
            packageName = "com.ubercab.driver",
            rawTitle = "Delivery • £7.32",
            rawText = "19 min (3.9 mi) away",
            receivedAtMs = 1234L,
            payGbp = 7.32,
            distanceMiles = 3.9,
            estimatedMinutes = 19
        )

        val result = LocalOfferScorer().score(offer)

        assertEquals(RecommendationDecision.ACCEPT, result.decision)
        assertEquals(90, result.score)
        assertEquals(1.88, result.estimatedPoundsPerMile!!, 0.01)
        assertEquals(23.12, result.estimatedPoundsPerHour!!, 0.01)
        assertEquals(12.32, result.requiredAverageSpeedMph!!, 0.01)
        assertEquals(12.32, result.theoreticalMinimumRideMinutes!!, 0.01)
        assertTrue(result.reasons.any { it.contains("19 mph") })
    }
}
