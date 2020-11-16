package ru.rescuesmstracker.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class AccumulationLocationListener : LocationListener {

    private var mostAccurateLocation: Location? = null

    @Synchronized
    override fun onLocationChanged(location: Location) {
        if (location.hasAccuracy()) {
            if (location.isMoreAccurateThan(mostAccurateLocation)) {
                mostAccurateLocation = location
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // do nothing
    }

    override fun onProviderEnabled(provider: String?) {
        // do nothing
    }

    override fun onProviderDisabled(provider: String?) {
        // do nothing
    }

    @Synchronized
    fun getMostAccurateLocation(): Location? = mostAccurateLocation

    private fun Location.isMoreAccurateThan(other: Location?): Boolean =
            other == null || accuracy < other.accuracy

}