package uk.jev.rider.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import uk.jev.rider.model.NormalisedOffer
import uk.jev.rider.model.OfferRecommendation

class OfferDatabase(context: Context) : SQLiteOpenHelper(
    context,
    "jev_rider.db",
    null,
    1
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE offers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                received_at_ms INTEGER NOT NULL,
                platform TEXT NOT NULL,
                package_name TEXT NOT NULL,
                raw_title TEXT,
                raw_text TEXT,
                pay_gbp REAL,
                distance_miles REAL,
                estimated_minutes INTEGER,
                decision TEXT NOT NULL,
                score INTEGER NOT NULL,
                pounds_per_mile REAL,
                pounds_per_hour REAL,
                reasons TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun insert(offer: NormalisedOffer, recommendation: OfferRecommendation): Long {
        val values = ContentValues().apply {
            put("received_at_ms", offer.receivedAtMs)
            put("platform", offer.platform.name)
            put("package_name", offer.packageName)
            put("raw_title", offer.rawTitle)
            put("raw_text", offer.rawText)
            offer.payGbp?.let { put("pay_gbp", it) }
            offer.distanceMiles?.let { put("distance_miles", it) }
            offer.estimatedMinutes?.let { put("estimated_minutes", it) }
            put("decision", recommendation.decision.name)
            put("score", recommendation.score)
            recommendation.estimatedPoundsPerMile?.let { put("pounds_per_mile", it) }
            recommendation.estimatedPoundsPerHour?.let { put("pounds_per_hour", it) }
            put("reasons", recommendation.reasons.joinToString(" | "))
        }
        return writableDatabase.insert("offers", null, values)
    }
}
