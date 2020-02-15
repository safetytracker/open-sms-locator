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

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import ru.rescuesmstracker.timer.ActivityTimer2
import ru.rescuesmstracker.timer.model.ScheduledSmsModel
import ru.rst.rescuesmstracker.R

object ActiveNotificationController {

    const val FOREGROUND_NOTIFICATION_ID = 11

    private const val chanelId = "active_notification_chanel"

    fun createForegroundNotification(context: Context): Notification {
        val notificationManager = notificationManager(context)

        val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, ActivityTimer2::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel(notificationManager, context)
        }

        return NotificationCompat.Builder(context, chanelId)
                .setContentTitle(context.getString(R.string.foreground_notification_title))
                .setSmallIcon(R.drawable.ic_active_notification)
                .setColor(ContextCompat.getColor(context, R.color.amber_600))
                .setContentIntent(appPendingIntent)
                .apply {
                    if (ScheduledSmsModel.isSmsSendingEnabled(context)) {
                        setContentText(context.getString(R.string.foreground_notification_periodic_sms_subtitle))
                    }
                }
                .build()
    }

    fun updateForegroundNotification(context: Context) {
        val notificationManager = notificationManager(context)
        if (RSTForegroundService.started) {
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, createForegroundNotification(context))
        } else {
            notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel(notificationManager: NotificationManager, context: Context) {
        val channelId = chanelId
        val channelName = context.getString(R.string.foreground_notification_chanel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        notificationChannel.enableLights(false)
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun notificationManager(context: Context): NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}