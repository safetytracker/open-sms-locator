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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import ru.rescuesmstracker.ActiveNotificationController.FOREGROUND_NOTIFICATION_ID
import ru.rescuesmstracker.ActiveNotificationController.createForegroundNotification

class RSTForegroundService : Service() {

    companion object {

        var started: Boolean = false
            private set

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(buildIntent(context))
            } else {
                context.startService(buildIntent(context))
            }
            started = true
            context.servicePreferences.started = true
        }

        fun stop(context: Context) {
            context.stopService(buildIntent(context))
            started = false
            context.servicePreferences.started = false
        }

        fun ping(context: Context): Boolean {
            if (context.servicePreferences.started) {
                start(context)
                return true
            }
            return false
        }

        private fun buildIntent(context: Context) = Intent(context, RSTForegroundService::class.java)

        private val Context.servicePreferences: ServicePreferences
            get() = ServicePreferences(
                    getSharedPreferences("RSTForegroundService", Context.MODE_PRIVATE)
            )

        private class ServicePreferences(val prefs: SharedPreferences) {

            var started: Boolean
                get() = prefs.getBoolean(KEY_STARTED, false)
                set(value) = prefs.edit().putBoolean(KEY_STARTED, value).apply()

        }
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException()
    }

    override fun onCreate() {
        super.onCreate()
        started = true
    }

    override fun onDestroy() {
        super.onDestroy()
        started = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification(this))

        return START_STICKY
    }

}

private const val KEY_STARTED = "started"