/**
 * Copyright (C) 2020 Safety Tracker
 *
 * This file is part of Open SMS Locator
 *
 * Open SMS Locator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Open SMS Locator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open SMS Locator. If not, see <https://www.gnu.org/licenses/>.
 */

package ru.rescuesmstracker

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import ru.rescuesmstracker.analytics.Analytics
import ru.rescuesmstracker.analytics.events.LocationFoundEvent
import ru.rescuesmstracker.settings.RSTPreferences
import java.util.concurrent.TimeUnit

object LocationProvider {

    interface LocationCallback {
        fun onReceivedLocation(location: Location, isLastKnown: Boolean)
        fun onFailedToGetLocation()
    }

    private val timeoutMillis = TimeUnit.SECONDS.toMillis(30)
    private const val logTag = "LocationProvider"

    fun currentLocation(context: Context, externalLocationListener: LocationCallback) {
        try {
            val wrappedLocationListener = object : LocationCallback {
                override fun onReceivedLocation(location: Location, isLastKnown: Boolean) {
                    RSTPreferences.putLastSmsAccuracy(context, location.accuracy)
                    externalLocationListener.onReceivedLocation(location, isLastKnown)
                }

                override fun onFailedToGetLocation() {
                    externalLocationListener.onFailedToGetLocation()
                }
            }
            val locationManager = locationManager(context)
            val providers = locationManager.getProviders(true)
            val locationListener = object : AccumulativeTimeoutLocationListenerWrapper(
                    timeoutMillis, wrappedLocationListener, providers.size) {
                override fun onRequestExpired() {
                    val location = getBestLastKnownLocation(locationManager)
                    if (location == null) {
                        Log.i(logTag, "Last known location not found")
                        wrappedLocationListener.onFailedToGetLocation()
                    } else {
                        Log.i(logTag, "Last known location found: $location")
                        wrappedLocationListener.onReceivedLocation(location, true)
                    }
                }
            }.startRequest()

            if (providers.isEmpty()) {
                locationListener.onRequestExpired()
            } else {
                providers.forEach { locationManager.requestSingleUpdate(it, locationListener, null) }
            }
        } catch (se: SecurityException) {
            PermissionsActivity.checkPermissions(context)
        }
    }

    fun requestLocationEnabling(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val packageManager = context.packageManager
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent)
        } else {
            // system can't handle request location intent. Do nothing in this case
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = locationManager(context)
        return try {
            // passive provider is always enabled. That's why it's passive :)
            locationManager.allProviders.any {
                LocationManager.PASSIVE_PROVIDER != it && locationManager.isProviderEnabled(it)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getMostAccurateLocation(lhs: Location, rhs: Location?): Location {
        return if (rhs == null) {
            lhs
        } else {
            if (lhs.hasAccuracy() && rhs.hasAccuracy()) {
                if (lhs.accuracy < rhs.accuracy) {
                    lhs
                } else {
                    rhs
                }
            } else if (!lhs.hasAccuracy()) {
                rhs
            } else {
                lhs
            }
        }
    }

    private fun getBestLastKnownLocation(locationManager: LocationManager): Location? = try {
        val providers = locationManager.allProviders
        if (providers.isEmpty()) {
            null
        } else {
            var resultLocation: Location? = null
            providers.forEach {
                val lastKnown = locationManager.getLastKnownLocation(it)
                if (lastKnown != null) {
                    resultLocation = getMostAccurateLocation(lastKnown, resultLocation)
                }
            }
            resultLocation
        }
    } catch (se: SecurityException) {
        null
    }

    private fun locationManager(context: Context): LocationManager = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private abstract class AccumulativeTimeoutLocationListenerWrapper(
            private val timeout: Long,
            private val locationListener: LocationCallback,
            private val resultCount: Int
    ) : LocationListenerAdapter() {
        private val handler = Handler()
        private val expirationCallback = {
            if (shouldExpire) {
                isExpired = true
                performRequestExpiration()
            }
        }

        private var startTimeMillis: Long = 0L
        private var isExpired = false
        private var shouldExpire = true
        private var currentBestLocation: Location? = null
        private var receivedLocationsCount = 0

        fun performRequestExpiration() {
            if (currentBestLocation == null) {
                Log.d(logTag, "Location request expired")
                onRequestExpired()
            } else {
                Log.d(logTag, "Location proceeded with $currentBestLocation")
                locationListener.onReceivedLocation(currentBestLocation!!, false)
            }
        }

        abstract fun onRequestExpired()

        fun startRequest(): AccumulativeTimeoutLocationListenerWrapper {
            handler.postDelayed(expirationCallback, timeout)
            Log.d(logTag, "Requesting location")
            startTimeMillis = RSTSystem.currentTimeMillis()
            return this
        }

        override fun onLocationChanged(location: Location?) {
            receivedLocationsCount += 1
            Log.d(logTag, "Expired=$isExpired. Got location: $location")
            if (!isExpired) {
                if (location != null) {
                    currentBestLocation = getMostAccurateLocation(location, currentBestLocation)
                }

                if (receivedLocationsCount == resultCount && currentBestLocation != null) {
                    shouldExpire = false
                    handler.removeCallbacks(expirationCallback)
                    locationListener.onReceivedLocation(currentBestLocation!!, false)
                }
            }

            // we want to track how much time we need to get the location
            Analytics.track(LocationFoundEvent(RSTSystem.currentTimeMillis() - startTimeMillis,
                    location?.provider))
        }
    }

    open class LocationListenerAdapter : LocationListener {
        override fun onLocationChanged(location: Location?) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }
}