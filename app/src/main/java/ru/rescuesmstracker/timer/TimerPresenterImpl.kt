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

import android.content.Context
import ru.rescuesmstracker.LocationProvider
import ru.rescuesmstracker.RSTForegroundService
import ru.rescuesmstracker.RSTSystem
import ru.rescuesmstracker.contacts.ContactsController
import ru.rescuesmstracker.data.Sms
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rescuesmstracker.timer.model.ForceSmsModel
import ru.rescuesmstracker.timer.model.ScheduledSmsModel
import ru.rescuesmstracker.widget.RSTTimerView

class TimerPresenterImpl(private val context: Context,
                         private var timerView: TimerView2) : TimerPresenter2 {

    private val smsListener: ForceSmsModel.SmsListener = object : ForceSmsModel.SmsListener {
        override fun onStatusChanged(status: Sms.Status, totalSmsIdsList: List<String>) {
            timerView.forceSmsSendingStatusUpdated(status)
        }

        override fun onAllSmsSent() {
            timerView.forceSmsSendingStatusUpdated(Sms.Status.SENT)
        }
    }
    private var task: ForceSmsModel.Task? = null
    private var allowPeriodic: Boolean = false

    override var enabled: Boolean = RSTForegroundService.started
        set(value) {
            field = value
            if (value) {
                RSTForegroundService.start(context)
            } else {
                RSTForegroundService.stop(context)
            }
            update()
        }

    override fun onStart() {
        update()
    }

    override fun onStop() {
        task?.onDestroy()
    }

    override fun toggleSmsSending() {
        if (!enabled) {
            throw IllegalStateException("Cannot toggle sms sending while main toggle is off")
        }
        if (ScheduledSmsModel.isSmsSendingEnabled(context)) {
            stopSmsSending()
        } else if (allowPeriodic) {
            startSmsSending()
        }
    }

    override fun forceSendSms() {
        task = ForceSmsModel.forceSendLocation(context, Sms.Type.LOCATION_FORCE,
                ContactsController.loadAllContacts(), smsListener)
        timerView.forceSmsSendingStatusUpdated(Sms.Status.SENDING)
    }

    override fun getTimerUpdate(timerMax: Int) {
        val currentSMS = ScheduledSmsModel.getCurrentlyProcessingSms()

        if (currentSMS != null) {
            val currentSMSTimestamp = when {
                currentSMS.sentTimestamp != 0L -> currentSMS.sentTimestamp
                currentSMS.sendingAttemptTimestamp != 0L -> currentSMS.sendingAttemptTimestamp
                else -> currentSMS.scheduledTimestamp
            }

            val previousTimestamp = currentSMSTimestamp - RSTPreferences.getSmsSendInterval(context)
            val currentTimeNormalized = RSTSystem.currentTimeMillis() - previousTimestamp
            val currentSMSTimestampNormalized = currentSMSTimestamp - previousTimestamp

            val progress = ((currentTimeNormalized / currentSMSTimestampNormalized.toDouble()) * timerMax)
                    .toFloat()
            val previousSms = ScheduledSmsModel.getPreviouslyProcessedSms()
            if (previousSms == null) {
                timerView.onGotData(RSTTimerView.State.WAITING, 0, progress)
            } else {
                timerView.onGotData(when (previousSms.getStatus()) {
                    Sms.Status.SENT -> RSTTimerView.State.WAITING
                    Sms.Status.FAILED_TO_SEND -> RSTTimerView.State.FAILED_TO_SEND
                    Sms.Status.SENDING -> RSTTimerView.State.SENDING_SMS
                }, RSTSystem.currentTimeMillis() - previousSms.sentTimestamp, progress)
            }
        }

        timerView.onUpdateStatus(RSTPreferences.getLastSmsAccuracy(context),
                LocationProvider.isLocationEnabled(context))
    }

    private fun update() {
        val contact = ContactsController.loadTopContact()
        allowPeriodic = (contact != null) && enabled

        if (enabled) {
            timerView.showEnabled()
            timerView.setContact(contact)

            if (contact == null) {
                stopSmsSending()
            } else {
                if (ScheduledSmsModel.isSmsSendingEnabled(context)) {
                    timerView.startTimer()
                } else {
                    timerView.stopTimer()
                }
            }
        } else {
            stopSmsSending()
            timerView.showDisabled()
        }
    }

    private fun startSmsSending() {
        ScheduledSmsModel.startSmsSending(context)
        timerView.startTimer()
    }

    private fun stopSmsSending() {
        ScheduledSmsModel.stopSmsSending(context)
        timerView.stopTimer()
    }
}