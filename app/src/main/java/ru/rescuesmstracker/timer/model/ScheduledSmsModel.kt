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

package ru.rescuesmstracker.timer.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import io.realm.Realm
import io.realm.Sort
import ru.rescuesmstracker.ActiveNotificationController
import ru.rescuesmstracker.RSTSystem
import ru.rescuesmstracker.contacts.ContactsController
import ru.rescuesmstracker.data.Sms
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rescuesmstracker.timer.SmsActionSendReceiver
import java.lang.IllegalArgumentException
import java.util.*

object ScheduledSmsModel : BaseSmsModel() {

    const val logTag = "ScheduledSmsModel"

    private val realm = Realm.getDefaultInstance()

    /**
     * Starts sms auto-sending
     * This buddy prepares sms instances in the storage and marks them as [Sms.Status.SENDING], shows
     * notification, schedules sending to [AlarmManager]
     */
    fun startSmsSending(context: Context) {
        val time = RSTSystem.currentTimeMillis()
        val triggerTime = time + RSTPreferences.getSmsSendInterval(context)
        schedule(context, triggerTime)
    }

    private fun schedule(context: Context, triggerTime: Long) {
        if (triggerTime <= 0L) {
            throw IllegalArgumentException("triggerTime must be greater than zero")
        }
        RSTPreferences.putSmsSendEnabled(context, true)
        RSTPreferences.putSmsSendTriggerTime(context, triggerTime)

        realm.beginTransaction()
        val pendingIntent = createSmsPendingIntent(context,
                ContactsController.loadAllContacts().map { contact ->
                    realm.copyToRealm(Sms(contact, triggerTime, "", Sms.Type.LOCATION_SCHEDULED))
                })
        realm.commitTransaction()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        ActiveNotificationController.updateForegroundNotification(context)
        Log.d(logTag, "startSmsSending with triggerTime=${Date(triggerTime)}")
    }

    /**
     * Stops sms auto-sending.
     * This buddy remove all pending intents from the [AlarmManager], hiding notification and
     * removes all currently processing sms (queryByStatus([Sms.Status.SENDING])) from the storage.
     */
    fun stopSmsSending(context: Context) {
        RSTPreferences.putSmsSendEnabled(context, false)
        RSTPreferences.putSmsSendTriggerTime(context, 0L)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createSmsPendingIntent(context))
        realm.executeTransaction {
            getCurrentlyProcessingSms()?.deleteFromRealm()
        }
        ActiveNotificationController.updateForegroundNotification(context)
    }

    /**
     * @return true if auto-sending is ON and false the otherwise
     */
    fun isSmsSendingEnabled(context: Context): Boolean = RSTPreferences.isSmsSendEnabled(context)

    fun reschedule(context: Context): Boolean {
        if (isSmsSendingEnabled(context)) {
            val triggerTime = RSTPreferences.getSmsSendTriggerTime(context)
            if (triggerTime > 0L) {
                schedule(context, triggerTime)
            } else {
                startSmsSending(context)
            }
            return true
        }
        return false
    }

    fun getCurrentlyProcessingSms(): Sms? = queryByStatus(Sms.Status.SENDING)

    fun getPreviouslyProcessedSms(): Sms? = queryByStatus(Sms.Status.SENT, Sms.Status.FAILED_TO_SEND)

    private fun queryByStatus(vararg statuses: Sms.Status): Sms? {
        val query = realm.where(Sms::class.java)
        query.equalTo("type", Sms.Type.LOCATION_SCHEDULED.name)
        if (statuses.isNotEmpty()) {
            query.equalTo("status", statuses[0].name)
        }
        if (statuses.size > 1) {
            for (i in (1 until statuses.size)) {
                query.or().equalTo("status", statuses[0].name)
            }
        }
        val sortedSms = query.findAllSorted("scheduledTimestamp", Sort.DESCENDING)
        return if (sortedSms.isEmpty()) {
            null
        } else {
            sortedSms[0]
        }
    }

    /**
     * Create sms pending intent to use it with [AlarmManager]. It put sms ids to extra for
     * [SmsActionSendReceiver] if [smsList] is non-empty
     */
    private fun createSmsPendingIntent(context: Context, smsList: List<Sms> = emptyList()): PendingIntent {
        val intent = Intent(context, SmsActionSendReceiver::class.java)
        intent.action = SmsActionSendReceiver.actionSendSms
        if (smsList.isNotEmpty()) {
            intent.putExtra(SmsActionSendReceiver.smsIdsKey, smsList.map { sms -> sms.id }.toTypedArray())
        }
        return PendingIntent.getBroadcast(
                context.applicationContext, 1313, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT)
    }
}