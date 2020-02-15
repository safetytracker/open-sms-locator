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

package ru.rescuesmstracker.timer

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import io.realm.Realm
import ru.rescuesmstracker.LocationProvider
import ru.rescuesmstracker.data.Sms
import ru.rescuesmstracker.onboarding.FormatUtils
import ru.rescuesmstracker.timer.model.BaseSmsModel
import ru.rescuesmstracker.timer.model.ScheduledSmsModel

/**
 * This receiver is posted to [AlarmManager] to schedule sms sending.
 */
class SmsActionSendReceiver : BroadcastReceiver() {

    companion object {
        val actionSendSms = "ru.rescuesmstracker.timer.SmsActionSendReceiver.actionSendSms"
        val smsIdsKey = "ru.rescuesmstracker.timer.SmsActionSendReceiver.smsIdsKey"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && actionSendSms == intent.action) {
            if (!ScheduledSmsModel.isSmsSendingEnabled(context)) {
                return
            }
            val realm = Realm.getDefaultInstance()
            Log.d(ScheduledSmsModel.logTag, "Received send sms intent")
            val smsId = intent.getStringArrayExtra(smsIdsKey) ?: return
            val smsList = realm.where(Sms::class.java).`in`("id", smsId).findAll()
            if (smsList.size == 0) {
                Log.e(ScheduledSmsModel.logTag, "Failed to get sms for deliveredSmsId=$smsId")
            } else {
                LocationProvider.currentLocation(context, object : LocationProvider.LocationCallback {
                    override fun onReceivedLocation(location: Location, isLastKnown: Boolean) {
                        smsList.forEach { sms ->
                            realm.executeTransaction {
                                sms.text = FormatUtils(context).formatLocationSms(location, isLastKnown)
                            }
                            BaseSmsModel.performLocationSmsSending(context, sms)
                        }
                    }

                    override fun onFailedToGetLocation() {
                    }
                })
            }
        }
    }
}