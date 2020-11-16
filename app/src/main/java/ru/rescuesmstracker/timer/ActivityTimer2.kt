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

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.a_timer_2.*
import kotlinx.android.synthetic.main.content_disabled.*
import kotlinx.android.synthetic.main.content_enabled.*
import kotlinx.android.synthetic.main.content_enabled.locationStatusBar
import kotlinx.android.synthetic.main.content_enabled.rstTimer
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.data.Sms
import ru.rescuesmstracker.location.LocationProvider
import ru.rescuesmstracker.settings.ActivitySettings
import ru.rescuesmstracker.widget.BaseRSTActivity
import ru.rescuesmstracker.widget.LocationStatusBar
import ru.rescuesmstracker.widget.RSTTimerView
import ru.rst.rescuesmstracker.R

class ActivityTimer2 : BaseRSTActivity(), TimerView2 {

    private lateinit var presenter: TimerPresenter2

    override fun createActivity(savedInstanceState: Bundle?) {
        super.createActivity(savedInstanceState)

        setContentView(R.layout.a_timer_2)

        initDisabledView()
        initEnabledView()

        presenter = TimerPresenterImpl(this, this)

        switchbutton_main.setOnCheckedChangeListener { _, enabled ->
            presenter.enabled = enabled
        }

        rstTimer.setOnClickListener {
            presenter.toggleSmsSending()
        }
        rstTimer.getUpdate = {
            presenter.getTimerUpdate(rstTimer.max)
        }

        locationStatusBar.setOnClickListener {
            if (locationStatusBar.status == LocationStatusBar.Status.LOCATION_DISABLED) {
                LocationProvider.requestLocationEnabling(this@ActivityTimer2)
            }
        }

        force_send_btn.setOnClickListener {
            presenter.forceSendSms()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun showEnabled() {
        content_disabled.visibility = View.INVISIBLE
        content_enabled.visibility = View.VISIBLE
        switchbutton_main.setCheckedImmediatelyNoEvent(true)
    }

    override fun showDisabled() {
        content_disabled.visibility = View.VISIBLE
        content_enabled.visibility = View.INVISIBLE
        switchbutton_main.setCheckedImmediatelyNoEvent(false)
    }

    override fun onGotData(status: RSTTimerView.State, lastSmsSentTimestamp: Long, progress: Float) {
        rstTimer.progress = progress
        rstTimer.setState(status, lastSmsSentTimestamp)
    }

    override fun onUpdateStatus(accuracy: Float, isLocationEnabled: Boolean) {
        locationStatusBar.onUpdateStatus(accuracy, isLocationEnabled)
    }

    override fun startTimer() {
        rstTimer.start()
        play_btn.playing = true
    }

    override fun stopTimer() {
        rstTimer.stop()
        play_btn.playing = false
    }

    override fun setContact(contact: Contact?) {
        contact_view.setContact(contact)
        force_send_container.visibility = if (contact == null) View.INVISIBLE else force_send_btn.visibility
    }

    override fun forceSmsSendingStatusUpdated(status: Sms.Status) {
        if (status == Sms.Status.SENDING) {
            force_send_btn.visibility = View.INVISIBLE
            force_send_progress.visibility = View.VISIBLE
        } else {
            force_send_btn.visibility = View.VISIBLE
            force_send_progress.visibility = View.INVISIBLE
        }
    }

    private fun initDisabledView() {
        btn_disabled_settings.setOnClickListener {
            startActivity(Intent(this, ActivitySettings::class.java))
        }
    }

    private fun initEnabledView() {
        btn_enabled_settings.setOnClickListener {
            startActivity(Intent(this, ActivitySettings::class.java))
        }
    }
}