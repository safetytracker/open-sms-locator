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

    private val listener = AccumulationLocationListener()
    private val locationManager = context.getLocationManager()

    fun start() {
        if (!context.hasLocationPermissions()) {
            callback.onRetrieveLocationFailed()
            return
        }
        val providers = locationManager.getProviders(true)
        if (providers.isEmpty()) {
            callback.onRetrieveLocationFailed()
        } else {
            providers.forEach { locationManager.requestLocationUpdatesSafe(it, listener) }
            startSlicing(listener)
        }
    }

    private fun startSlicing(accumulationLocationListener: AccumulationLocationListener) {
        val mainHandler = Handler(Looper.getMainLooper())
        val startTime = RSTSystem.currentTimeMillis()
        val sliceRunnable = object : Runnable {
            override fun run() {
                val mostAccurate = accumulationLocationListener.getMostAccurateLocation()
                if (mostAccurate != null && mostAccurate.accuracy < REQUIRED_ACCURACY_METERS) {
                    val elapsedTime = RSTSystem.currentTimeMillis() - startTime
                    callback.onMostAccurateLocationRetrieved(mostAccurate, elapsedTime)
                    stopSlicing()
                } else if (RSTSystem.currentTimeMillis() - startTime <= MAX_WAITING_TIME_MS) {
                    mainHandler.postDelayed(this, SLICE_PERIOD_MS)
                } else {
                    callback.onRetrieveLocationExpired(mostAccurate)
                    stopSlicing()
                }
            }
        }
        mainHandler.postDelayed(sliceRunnable, SLICE_PERIOD_MS)
    }

    private fun stopSlicing() {
        locationManager.removeUpdates(listener)
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