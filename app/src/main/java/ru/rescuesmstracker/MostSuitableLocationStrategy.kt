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

import android.location.Location
import android.os.Build

sealed class MostSuitableLocationStrategy {

    abstract fun getMostSuitableLocation(lhs: Location, rhs: Location?): Location

    object MostAccurateLocationStrategy : MostSuitableLocationStrategy() {

        override fun getMostSuitableLocation(lhs: Location, rhs: Location?): Location {
            return if (rhs == null) {
                lhs
            } else {
                if (lhs.hasAccuracy() && rhs.hasAccuracy()) {
                    if (lhs.accuracy < rhs.accuracy) {
                        lhs
                    } else {
                        rhs
                    }
                } else if (!lhs.hasAccuracy()) {
                    rhs
                } else {
                    lhs
                }
            }
        }

    }

    object MostRecentLocationStrategy : MostSuitableLocationStrategy() {

        override fun getMostSuitableLocation(lhs: Location, rhs: Location?): Location {
            return if (rhs == null) {
                lhs
            } else {
                if (lhs.timeToCompare > rhs.timeToCompare) {
                    lhs
                } else {
                    rhs
                }
            }
        }

        private val Location.timeToCompare: Long
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                elapsedRealtimeNanos
            } else {
                time
            }

    }

}