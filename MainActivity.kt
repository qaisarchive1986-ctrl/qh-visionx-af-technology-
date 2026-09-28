package com.qhtechnology.wifi

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.qhtechnology.wifi.data.AppDatabase
import com.qhtechnology.wifi.data.WifiNetwork
import com.qhtechnology.wifi.location.LocationTracker
import com.qhtechnology.wifi.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wifi: WifiManager
    private lateinit var location: LocationTracker
    private val db by lazy { AppDatabase.get(this) }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (hasLocationPermission()) {
                location.current { saveScan(wifi.scanResults, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        location = LocationTracker(this)

        binding.scanButton.setOnClickListener { startScan() }
        binding.mapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        binding.searchButton.setOnClickListener {
            observe(binding.searchBox.text.toString())
        }

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        } else observe()
    }

    private fun hasLocationPermission() = location.hasPermission()

    private fun startScan() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show()
            return
        }
        binding.status.text = "Scanning Wi-Fi…"
        wifi.startScan()
    }

    private fun saveScan(results: List<ScanResult>, loc: android.location.Location?) {
        lifecycleScope.launch {
            results.forEach { r ->
                val old = db.wifiDao().find(r.BSSID)
                val now = System.currentTimeMillis()
                val item = WifiNetwork(
                    id = old?.id ?: 0,
                    ssid = r.SSID,
                    bssid = r.BSSID,
                    signalDbm = r.level,
                    frequencyMhz = r.frequency,
                    latitude = loc?.latitude ?: old?.latitude,
                    longitude = loc?.longitude ?: old?.longitude,
                    altitude = if (loc?.hasAltitude() == true) loc.altitude else old?.altitude,
                    accuracyMeters = if (loc?.hasAccuracy() == true) loc.accuracy else old?.accuracyMeters,
                    discoveredAt = now
                )
                if (old == null) db.wifiDao().insert(item) else db.wifiDao().update(item)
            }
            binding.status.text = "${results.size} networks saved"
        }
    }

    private fun observe(q: String = "") {
        lifecycleScope.launch {
            val flow = if (q.isBlank()) db.wifiDao().observeAll() else db.wifiDao().search(q)
            flow.collect { show(it) }
        }
    }

    private fun show(items: List<WifiNetwork>) {
        binding.networkList.removeAllViews()
        binding.status.text = "Saved networks: ${items.size}"
        items.forEach { n ->
            binding.networkList.addView(TextView(this).apply {
                text = buildString {
                    append("SSID: ${n.ssid}\n")
                    append("BSSID: ${n.bssid}\n")
                    append("Signal: ${n.signalDbm} dBm\n")
                    append("Frequency: ${n.frequencyMhz} MHz\n")
                    if (n.latitude != null && n.longitude != null)
                        append("GPS: %.6f, %.6f\n".format(n.latitude, n.longitude))
                    else append("GPS: unavailable\n")
                    n.accuracyMeters?.let { append("Accuracy: %.1f m".format(it)) }
                }
                textSize = 15f
                setPadding(0, 16, 0, 16)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }
}
