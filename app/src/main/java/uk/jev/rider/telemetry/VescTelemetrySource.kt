package uk.jev.rider.telemetry

import uk.jev.rider.model.BikeTelemetry

interface VescTelemetrySource {
    suspend fun latest(): BikeTelemetry
}

class NoVescTelemetrySource : VescTelemetrySource {
    override suspend fun latest(): BikeTelemetry = BikeTelemetry(available = false)
}

/*
 * V1 intentionally stops here.
 *
 * Implement read-only Flipsky 75100 V1 telemetry only after confirming the
 * exact Bluetooth module and protocol exposed by the user's bike.
 * This integration must remain optional.
 */
