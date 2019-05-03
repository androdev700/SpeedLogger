package com.andro.speedlogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), IBaseGpsListener {

    private var state = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_stop_btn.setOnClickListener {
            val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            state = !state
            if (state) {
                start_stop_btn.text = "Stop"
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
                    this.updateSpeed(null)
                }
            } else {
                start_stop_btn.text = "Start"
                locationManager.removeUpdates(this)
            }
        }
    }

    override fun finish() {
        super.finish()
        System.exit(0)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSpeed(location: CLocation?) {
        val nCurrentSpeed: Float
        if (location != null) {
            nCurrentSpeed = location.speed
            val fmt = Formatter(StringBuilder())
            fmt.format(Locale.US, "%5.1f", nCurrentSpeed)
            var strCurrentSpeed = fmt.toString()
            strCurrentSpeed = strCurrentSpeed.replace(' ', '0')
            val strUnits = "km/hr"
            txtCurrentSpeed.text = "$strCurrentSpeed $strUnits"
            gauge.speedTo(strCurrentSpeed.toFloat(), 500)
        } else {
            txtCurrentSpeed.text = "Not receiving updates.."
        }
    }

    override fun onLocationChanged(location: Location) {
        val myLocation = CLocation(location)
        this.updateSpeed(myLocation)
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onGpsStatusChanged(event: Int) {

    }
}
