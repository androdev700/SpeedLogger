package com.andro.speedlogger

import android.location.Location

class CLocation constructor(location: Location) : Location(location) {

    override fun getSpeed(): Float {
        return super.getSpeed() * 3.6f * 2
    }
}