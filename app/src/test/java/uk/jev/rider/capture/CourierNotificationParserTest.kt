package uk.jev.rider.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.jev.rider.model.CourierPlatform

class CourierNotificationParserTest {
    @Test
    fun parsesConfirmedUberNotificationFormat() {
        val offer = CourierNotificationParser.parseText(
            packageName = "com.ubercab.driver",
            title = "Delivery • £7.32",
            text = "19 min (3.9 mi) away",
            receivedAtMs = 1234L
        )

        assertNotNull(offer)
        offer!!

        assertEquals(CourierPlatform.UBER_EATS, offer.platform)
        assertEquals(7.32, offer.payGbp!!, 0.001)
        assertEquals(3.9, offer.distanceMiles!!, 0.001)
        assertEquals(19, offer.estimatedMinutes)
    }
}
