package ru.rescuesmstracker.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import ru.rescuesmstracker.RSTSystem
import ru.rescuesmstracker.extensions.getLocationManager
import ru.rescuesmstracker.extensions.isPermissionGranted
import java.util.concurrent.TimeUnit

class CurrentLocationRetriever(
        private val context: Context,
        private val callback: Callback
) {

    fun start() {
        if (!context.hasLocationPermissions()) {
            callback.onRetrieveLocationFailed()
            return
        }
        val locationManager = context.getLocationManager()
        val providers = locationManager.getProviders(true)
        if (providers.isEmpty()) {
            callback.onRetrieveLocationFailed()
        } else {
            val listener = AccumulationLocationListener()
            providers.forEach { locationManager.requestLocationUpdatesSafe(it, listener) }
            startWaiting(listener)
        }
    }

    private fun startWaiting(accumulationLocationListener: AccumulationLocationListener) {
        val mainHandler = Handler(Looper.getMainLooper())
        val startTime = RSTSystem.currentTimeMillis()
        val sliceRunnable = object : Runnable {
            override fun run() {
                val locations = accumulationLocationListener.getAccumulated()
                val mostAccurate = getMostAccurateLocation(locations)
                if (mostAccurate != null && mostAccurate.accuracy < REQUIRED_ACCURACY_METERS) {
                    val elapsedTime = RSTSystem.currentTimeMillis() - startTime
                    callback.onMostAccurateLocationRetrieved(mostAccurate, elapsedTime)
                } else if (RSTSystem.currentTimeMillis() - startTime <= MAX_WAITING_TIME_MS) {
                    mainHandler.postDelayed(this, SLICE_PERIOD_MS)
                } else {
                    callback.onRetrieveLocationExpired(mostAccurate)
                }
            }
        }
        mainHandler.postDelayed(sliceRunnable, SLICE_PERIOD_MS)
    }

    private fun getMostAccurateLocation(locations: List<Location>): Location? {
        var bestAccuracy: Float = Float.MAX_VALUE
        var bestLocation: Location? = null
        locations.asSequence()
                .filter { it.hasAccuracy() }
                .forEach {
                    if (it.accuracy < bestAccuracy) {
                        bestAccuracy = it.accuracy
                        bestLocation = it
                    }
                }
        return bestLocation
    }

    private fun Context.hasLocationPermissions(): Boolean =
            isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                    || isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun LocationManager.requestLocationUpdatesSafe(
            provider: String,
            listener: LocationListener
    ) {
        try {
            requestLocationUpdates(provider, MIN_UPDATE_INTERVAL_MS, MIN_DISTANCE_METERS, listener)
        } catch (e: SecurityException) {
            // TODO log exception
        }
    }

    interface Callback {
        fun onMostAccurateLocationRetrieved(location: Location, elapsedTime: Long)
        fun onRetrieveLocationFailed()
        fun onRetrieveLocationExpired(bestRetrievedLocation: Location?)
    }
}

private const val REQUIRED_ACCURACY_METERS = 100
private const val MIN_DISTANCE_METERS = 0.1f * REQUIRED_ACCURACY_METERS // 10% of required

private val MIN_UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5)
private val SLICE_PERIOD_MS = TimeUnit.SECONDS.toMillis(30)
private val MAX_WAITING_TIME_MS = TimeUnit.MINUTES.toMillis(5)