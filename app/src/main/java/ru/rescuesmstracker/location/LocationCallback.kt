package ru.rescuesmstracker.location

import android.location.Location

interface LocationCallback {

    fun onReceivedLocation(location: Location, isLastKnown: Boolean)

    fun onFailedToGetLocation()

}