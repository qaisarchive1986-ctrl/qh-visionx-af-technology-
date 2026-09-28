package com.qhtechnology.wifi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.qhtechnology.wifi.data.AppDatabase
import com.qhtechnology.wifi.databinding.ActivityMapBinding
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private val db by lazy { AppDatabase.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)
    }

    override fun onMapReady(g: GoogleMap) {
        map = g
        map.uiSettings.isZoomControlsEnabled = true
        lifecycleScope.launch {
            db.wifiDao().observeAll().collect { items ->
                map.clear()
                var first: LatLng? = null
                var count = 0
                items.forEach { n ->
                    val lat = n.latitude
                    val lon = n.longitude
                    if (lat != null && lon != null) {
                        val p = LatLng(lat, lon)
                        if (first == null) first = p
                        count++
                        map.addMarker(
                            MarkerOptions()
                                .position(p)
                                .title(if (n.ssid.isBlank()) "(Hidden network)" else n.ssid)
                                .snippet(
                                    "BSSID: ${n.bssid}\n" +
                                    "Signal: ${n.signalDbm} dBm\n" +
                                    "Frequency: ${n.frequencyMhz} MHz\n" +
                                    "GPS: %.6f, %.6f".format(lat, lon)
                                )
                        )
                    }
                }
                binding.mapStatus.text = "QH Technology • Mapped: $count"
                first?.let { map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
            }
        }
    }
}
