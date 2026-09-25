package uk.jev.rider

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import uk.jev.rider.config.RiderConfig

class MainActivity : Activity() {
    private lateinit var lastEvent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "JEV Rider Optimiser V1.1"
            textSize = 26f
        }

        val subtitle = TextView(this).apply {
            text = "Uber notification format loaded. Bike max speed: ${RiderConfig.BIKE_MAX_SPEED_MPH.toInt()} mph. Grant notification access, then inspect captured offers."
            textSize = 16f
        }

        val accessButton = Button(this).apply {
            text = "Open notification access"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val refreshButton = Button(this).apply {
            text = "Refresh last captured event"
            setOnClickListener { refreshLastEvent() }
        }

        lastEvent = TextView(this).apply {
            textSize = 14f
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(accessButton)
        root.addView(refreshButton)
        root.addView(lastEvent)

        setContentView(ScrollView(this).apply { addView(root) })
        refreshLastEvent()
    }

    override fun onResume() {
        super.onResume()
        refreshLastEvent()
    }

    private fun refreshLastEvent() {
        val value = getSharedPreferences("jev_rider", Context.MODE_PRIVATE)
            .getString("last_event", null)

        lastEvent.text = value ?: "No courier notification captured yet."
    }
}
