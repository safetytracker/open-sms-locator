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
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Handler
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rst.rescuesmstracker.BuildConfig
import java.util.concurrent.TimeUnit

interface RSTSmsManager {
    fun sendTextMessage(context: Context, destinationAddress: String, scAddress: String?, text: String, sentIntent: PendingIntent?, deliveryIntent: PendingIntent?)
    fun canSelectSubscription(context: Context): Boolean
    fun getActiveSimId(context: Context): SimInfo
    fun getAllAvailableSimIds(context: Context): List<SimInfo>

    companion object {
        const val defaultActiveSimId = -1

        fun get(): RSTSmsManager = if (BuildConfig.DEBUG) RSTSmsManager.DebugSmsManager() else RSTSmsManager.RealSmsManager()
    }

    private class DebugSmsManager : RSTSmsManager {

        val handler = Handler()
        var statusCode: Int = Activity.RESULT_OK

        override fun sendTextMessage(context: Context, destinationAddress: String, scAddress: String?, text: String, sentIntent: PendingIntent?, deliveryIntent: PendingIntent?) {
            handler.postDelayed({
                Log.d("DebugSmsManager", "sendTextMessage [ destinationAddress: $destinationAddress text: $text ]")
                sentIntent?.send(statusCode)
            }, TimeUnit.SECONDS.toMillis(3))
        }

        override fun canSelectSubscription(context: Context): Boolean {
            return true
        }

        override fun getActiveSimId(context: Context): SimInfo {
            return SimInfo(defaultActiveSimId, "debug_phone_number", "debug")
        }

        override fun getAllAvailableSimIds(context: Context): List<SimInfo> {
            return listOf(getActiveSimId(context))
        }
    }

    private open class RealSmsManager : RSTSmsManager {

        private val delegate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            RealSmsManagerDelegate22()
        } else {
            BaseRealSmsManagerDelegate()
        }

        override fun sendTextMessage(context: Context, destinationAddress: String, scAddress: String?, text: String, sentIntent: PendingIntent?, deliveryIntent: PendingIntent?) {
            try {
                delegate.sendTextMessage(context, destinationAddress, scAddress, text, sentIntent, deliveryIntent)
            } catch (se: SecurityException) {
                PermissionsActivity.checkPermissions(context)
            }
        }

        override fun canSelectSubscription(context: Context): Boolean = try {
            delegate.canSelectSubscription(context)
        } catch (se: SecurityException) {
            PermissionsActivity.checkPermissions(context)
            false
        }

        override fun getActiveSimId(context: Context): SimInfo = delegate.getActiveSimId(context)

        override fun getAllAvailableSimIds(context: Context): List<SimInfo> =
                delegate.getAllAvailableSimIds(context)

        companion object {
            protected fun deliverErrorIfPossible(sentIntent: PendingIntent?) {
                sentIntent?.send(SmsManager.RESULT_ERROR_RADIO_OFF)
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        private class RealSmsManagerDelegate22 : RSTSmsManager {
            override fun sendTextMessage(context: Context, destinationAddress: String, scAddress: String?, text: String, sentIntent: PendingIntent?, deliveryIntent: PendingIntent?) {
                val smsManager = getSmsManager(context)
                if (smsManager == null) {
                    deliverErrorIfPossible(sentIntent)
                } else {
                    smsManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent)
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            private fun getSmsManager(context: Context): SmsManager? {
                return try {
                    SmsManager.getSmsManagerForSubscriptionId(getActiveSimId(context).id)
                } catch (se: SecurityException) {
                    SmsManager.getDefault()
                }
            }

            override fun canSelectSubscription(context: Context): Boolean {
                // return true if there is more than one active sim
                return SubscriptionManager.from(context).activeSubscriptionInfoCount > 1
            }

            override fun getActiveSimId(context: Context): SimInfo {
                val selectedActive = RSTPreferences.getActiveSubscriptionId(context)
                val info = SubscriptionManager.from(context).getActiveSubscriptionInfo(if (selectedActive == defaultActiveSimId) {
                    SmsManager.getDefaultSmsSubscriptionId()
                } else {
                    selectedActive
                })
                return if (info == null) SimInfo(defaultActiveSimId, null, null)
                else SimInfo(info.subscriptionId, info.number, info.carrierName)
            }

            override fun getAllAvailableSimIds(context: Context): List<SimInfo> {
                return SubscriptionManager.from(context).activeSubscriptionInfoList
                        .map { SimInfo(it.subscriptionId, it.number, it.carrierName) }
            }
        }

        private class BaseRealSmsManagerDelegate : RSTSmsManager {
            override fun sendTextMessage(context: Context, destinationAddress: String, scAddress: String?, text: String, sentIntent: PendingIntent?, deliveryIntent: PendingIntent?) {
                val smsManager = SmsManager.getDefault()
                if (smsManager == null) {
                    deliverErrorIfPossible(sentIntent)
                } else {
                    smsManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent)
                }
            }

            override fun canSelectSubscription(context: Context): Boolean = false

            override fun getActiveSimId(context: Context): SimInfo = SimInfo(defaultActiveSimId,
                    (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number, "")

            override fun getAllAvailableSimIds(context: Context): List<SimInfo> {
                return listOf(getActiveSimId(context))
            }
        }
    }

    data class SimInfo(val id: Int, val number: String?, val carrierName: CharSequence?)
}