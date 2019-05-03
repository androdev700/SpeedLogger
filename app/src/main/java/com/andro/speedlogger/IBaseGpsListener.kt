package com.andro.speedlogger

import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.os.Bundle

interface IBaseGpsListener : LocationListener, GpsStatus.Listener {
    override fun onLocationChanged(location: Location)

    override fun onProviderDisabled(provider: String)

    override fun onProviderEnabled(provider: String)

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle)

    override fun onGpsStatusChanged(event: Int)
}