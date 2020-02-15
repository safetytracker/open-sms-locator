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

package ru.rescuesmstracker.analytics.trackers

import android.app.Application
import android.util.Log
import ru.rescuesmstracker.analytics.Event
import ru.rescuesmstracker.analytics.Tracker

class DebugTracker : Tracker {

    private var logTag = "DebugTracker"
    private var isReadyToUse = false

    override fun init(application: Application) {
        isReadyToUse = true
    }

    override fun track(event: Event) {
        if (isReadyToUse) {
            Log.d(logTag, "Event: $event")
        }
    }
}