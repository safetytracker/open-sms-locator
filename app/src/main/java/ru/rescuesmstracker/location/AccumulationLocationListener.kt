package ru.rescuesmstracker.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class AccumulationLocationListener : LocationListener {

    private val accumulatedLocations = arrayListOf<Location>()

    override fun onLocationChanged(location: Location) {
        synchronized(accumulatedLocations) {
            accumulatedLocations += location
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

    fun getAccumulated(): List<Location> = synchronized(accumulatedLocations) {
        ArrayList(accumulatedLocations)
    }

}