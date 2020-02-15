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

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ru.rescuesmstracker.timer.model.ScheduledSmsModel

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        if (RSTForegroundService.ping(context)) {
            Log.d(TAG, "Service started after device boot up")
        } else {
            Log.d(TAG, "Service NOT started after device boot up")
        }

        if (ScheduledSmsModel.reschedule(context)) {
            Log.d(TAG, "SMS scheduled after device boot up")
        } else {
            Log.d(TAG, "SMS NOT scheduled after device boot up")
        }
    }
}

private const val TAG = "BootReceiver"