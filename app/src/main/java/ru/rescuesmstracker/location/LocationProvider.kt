package ru.rescuesmstracker.location

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import ru.rescuesmstracker.analytics.Analytics
import ru.rescuesmstracker.analytics.events.LocationFoundEvent
import ru.rescuesmstracker.extensions.getLocationManager
import ru.rescuesmstracker.settings.RSTPreferences

object LocationProvider {

    fun currentLocation(context: Context, locationCallback: LocationCallback) {
        CurrentLocationRetriever(
                context = context,
                callback = CallbackAdapter(context, locationCallback)
        ).start()
    }

    fun requestLocationEnabling(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val packageManager = context.packageManager
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getLocationManager()
        return try {
            // passive provider is always enabled. That's why it's passive :)
            locationManager.allProviders.any {
                LocationManager.PASSIVE_PROVIDER != it && locationManager.isProviderEnabled(it)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getBestLastKnownLocation(locationManager: LocationManager): Location? = try {
        val providers = locationManager.allProviders
        if (providers.isEmpty()) {
            null
        } else {
            var resultLocation: Location? = null
            providers.asSequence()
                    .mapNotNull { locationManager.getLastKnownLocation(it) }
                    .forEach { lastKnown ->
                        resultLocation = getMostAccurateLocation(lastKnown, resultLocation)
                    }
            resultLocation
        }
    } catch (se: SecurityException) {
        null
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

    private class CallbackAdapter(
            private val context: Context,
            private val locationCallback: LocationCallback
    ) : CurrentLocationRetriever.Callback {

        override fun onMostAccurateLocationRetrieved(location: Location, elapsedTime: Long) {
            onReceivedLocation(location = location, isLastKnown = false)
            // we want to track how much time we need to get the location
            Analytics.track(LocationFoundEvent(elapsedTime, location.provider))
        }

        override fun onRetrieveLocationFailed() {
            locationCallback.onFailedToGetLocation()
        }

        override fun onRetrieveLocationExpired(bestRetrievedLocation: Location?) {
            if (bestRetrievedLocation != null) {
                onReceivedLocation(location = bestRetrievedLocation, isLastKnown = false)
            } else {
                val location = getBestLastKnownLocation(context.getLocationManager())
                if (location == null) {
                    locationCallback.onFailedToGetLocation()
                } else {
                    onReceivedLocation(location = location, isLastKnown = true)
                }
            }
        }

        private fun onReceivedLocation(location: Location, isLastKnown: Boolean) {
            RSTPreferences.putLastSmsAccuracy(context, location.accuracy)
            locationCallback.onReceivedLocation(location, isLastKnown)
        }

    }

}
