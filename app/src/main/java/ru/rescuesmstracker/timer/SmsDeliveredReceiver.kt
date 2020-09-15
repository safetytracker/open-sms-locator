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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import ru.rescuesmstracker.RSTSystem
import ru.rescuesmstracker.data.Sms
import ru.rescuesmstracker.timer.model.ForceSmsModel
import ru.rescuesmstracker.timer.model.ScheduledSmsModel
import ru.rst.rescuesmstracker.R

/**
 * This receiver is used to process sms sending result.
 * It updates [Sms.status] and [Sms.sentTimestamp] and schedules next sms sending
 */
class SmsDeliveredReceiver : BroadcastReceiver() {
    companion object {
        const val actionDeliverySms = "ru.rescuesmstracker.timer.SmsDeliveredReceiver.actionDeliverySms"
        const val smsIdKey = "ru.rescuesmstracker.timer.SmsDeliveredReceiver.smsIdKey"
        const val taskIdKey = "ru.rescuesmstracker.timer.SmsDeliveredReceiver.taskIdKey"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && actionDeliverySms == intent.action) {
            val realm = Realm.getDefaultInstance()
            val smsId = intent.getStringExtra(smsIdKey) ?: return
            val sms = realm.where(Sms::class.java).equalTo("id", smsId).findFirst()
            if (sms == null) {
                Log.e(ScheduledSmsModel.logTag, "Failed to get sms for deliveredSmsId=$smsId")
            } else {
                Log.d(ScheduledSmsModel.logTag, "Received sms status intent")
                val smsStatus = Sms.Status.fromResultCode(resultCode)
                realm.executeTransaction {
                    sms.sentTimestamp = RSTSystem.currentTimeMillis()
                    sms.setStatus(smsStatus)
                }
                if (smsStatus == Sms.Status.FAILED_TO_SEND) {
                    Toast.makeText(context, context.getString(R.string.sms_error_message), Toast.LENGTH_SHORT).show()
                }
                ForceSmsModel.notifyTask(context, intent.getStringExtra(taskIdKey), sms)
                // schedule next sms sending for scheduled sms
                if (sms.getType() == Sms.Type.LOCATION_SCHEDULED) {
                    ScheduledSmsModel.startSmsSending(context)
                }
            }
        }
    }
}