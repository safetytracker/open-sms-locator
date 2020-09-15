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

package ru.rescuesmstracker.settings

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import ru.rescuesmstracker.RSTSmsManager
import ru.rescuesmstracker.WhoCanRequestLocation
import ru.rescuesmstracker.extensions.getStringOrDefault
import ru.rescuesmstracker.extensions.getStringOrEmpty
import ru.rst.rescuesmstracker.R
import java.util.concurrent.TimeUnit

object RSTPreferences {

    const val customIntervalValue = Long.MIN_VALUE
    const val customSmsCountValue = Int.MIN_VALUE
    const val SMS_COUNT_INFINITE = Int.MAX_VALUE
    const val SMS_MAX_COUNT_DEFAULT_VALUE = SMS_COUNT_INFINITE

    val SMS_INTERVAL_DEFAULT_VALUE = TimeUnit.MINUTES.toMillis(30)
    private val COORDS_FORMAT_DEFAULT_VALUE = CoordsFormat.FORMAT_DEGREES

    enum class CoordsFormat(val exampleRes: Int) {
        FORMAT_DEGREES(R.string.format_degrees_example) {

            override fun format(location: Location): String {
                var lat = Location.convert(location.latitude, Location.FORMAT_DEGREES)
                lat = preprocessCoord(lat, false)
                lat = safeCut(lat, 5, ".")

                var lon = Location.convert(location.longitude, Location.FORMAT_DEGREES)
                lon = preprocessCoord(lon, true)
                lon = safeCut(lon, 5, ".")

                return "${determineSouthNorth(location.latitude)}$lat°" + " " +
                        "${determineEastWest(location.longitude)}$lon°"
            }

            /**
             * 22,26472 to 022.26472
             */
            override fun preprocessCoord(coord: String, shouldAddZero: Boolean): String {
                var result = if (coord.startsWith('-')) coord.substring(1) else coord
                result = super.preprocessCoord(result, shouldAddZero)
                if (result.substring(0, result.indexOf(".")).length == 2 && shouldAddZero) {
                    result = "0$result"
                }
                return result
            }
        },
        FORMAT_SECONDS(R.string.format_seconds_example) {
            override fun format(location: Location): String {
                var lat = Location.convert(location.latitude, Location.FORMAT_SECONDS)
                lat = preprocessCoord(lat, false)
                lat = safeCut(lat, 5, ".")

                var lon = Location.convert(location.longitude, Location.FORMAT_SECONDS)
                lon = preprocessCoord(lon, true)
                lon = safeCut(lon, 5, ".")

                return "${determineSouthNorth(location.latitude)}$lat\"" + " " +
                        "${determineEastWest(location.longitude)}$lon\""
            }

            /**
             * 22:17:57.26472 to 022°17'57.26
             */
            override fun preprocessCoord(coord: String, shouldAddZero: Boolean): String {
                var result = if (coord.startsWith('-')) coord.substring(1) else coord
                result = result
                        .replaceFirst(":", "°")
                        .replaceFirst(":", "'")
                if (result.substring(0, result.indexOf("°")).length == 2 && shouldAddZero) {
                    result = "0$result"
                }
                return super.preprocessCoord(result, shouldAddZero)
            }
        },
        FORMAT_MINUTES(R.string.format_minutes_example) {
            override fun format(location: Location): String {
                var lat = Location.convert(location.latitude, Location.FORMAT_MINUTES)
                lat = preprocessCoord(lat, false)
                lat = safeCut(lat, 5, ".")

                var lon = Location.convert(location.longitude, Location.FORMAT_MINUTES)
                lon = preprocessCoord(lon, true)
                lon = safeCut(lon, 5, ".")

                return "${determineSouthNorth(location.latitude)}$lat\'" + " " +
                        "${determineEastWest(location.longitude)}$lon\'"
            }

            /**
             * 22:17.26472 to 022°17.26'
             */
            override fun preprocessCoord(coord: String, shouldAddZero: Boolean): String {
                var result = if (coord.startsWith('-')) coord.substring(1) else coord
                result = result.replaceFirst(":", "°")
                if (result.substring(0, result.indexOf("°")).length == 2 && shouldAddZero) {
                    result = "0$result"
                }
                return super.preprocessCoord(result, shouldAddZero)
            }
        },
        FORMAT_GOOGLE_MAPS(R.string.format_google_maps_example) {
            override fun format(location: Location): String {
                var lat = Location.convert(location.latitude, Location.FORMAT_DEGREES)
                lat = preprocessCoord(lat, false)
                lat = safeCut(lat, 5, ".")

                var lon = Location.convert(location.longitude, Location.FORMAT_DEGREES)
                lon = preprocessCoord(lon, true)
                lon = safeCut(lon, 5, ".")

                val resultLocationString = "$lat,$lon"

                return "maps.google.com/?q=$resultLocationString"
            }

            /**
             * 22,26472 to 022.26472
             */
            override fun preprocessCoord(coord: String, shouldAddZero: Boolean): String {
                var result = super.preprocessCoord(coord, shouldAddZero)
                var sign = ""
                if (result.startsWith('-')) {
                    sign = "-"
                    result = result.substring(1)
                }
                if (result.substring(0, result.indexOf(".")).length == 2 && shouldAddZero) {
                    result = "0$result"
                }
                return sign + result
            }
        },
        FORMAT_NAVITEL(R.string.format_navitel_example) {
            override fun format(location: Location): String {
                val resultLocationString = FORMAT_DEGREES.format(location).replace("°", "")
                return "<NavitelLoc>$resultLocationString<N>(i'm here)"
            }
        };

        protected fun determineEastWest(coord: Double): String = if (coord > 0) "E" else "W"
        protected fun determineSouthNorth(coord: Double): String = if (coord > 0) "N" else "S"
        protected open fun preprocessCoord(coord: String, shouldAddZero: Boolean): String {
            // in some languages , is digit spacer. So we need to force replace it with .
            val result = coord.replace(",", ".")
            return if (result.indexOf('.') == -1) "$result.0" else result
        }

        protected fun safeCut(input: String, maxSymbols: Int, afterChar: String): String = try {
            val afterCharIndex = input.lastIndexOf(afterChar) + 1
            if (input.length <= afterCharIndex + maxSymbols) {
                val builder = StringBuilder(input)
                for (i in 0 until ((afterCharIndex + maxSymbols) - input.length)) {
                    builder.append("0")
                }
                builder.toString()
            } else {
                input.substring(0, afterCharIndex + maxSymbols)
            }
        } catch (e: StringIndexOutOfBoundsException) {
            input
        }

        abstract fun format(location: Location): String
    }

    fun putCodeWord(context: Context, word: String) {
        obtainPrefs(context).edit().putString("code_word", word).apply()
    }

    fun getCodeWord(context: Context): String = obtainPrefs(context).getStringOrEmpty("code_word")

    fun setSMSSendInterval(context: Context, intervalMillis: Long) {
        obtainPrefs(context).edit().putLong("sms_send_interval", intervalMillis).apply()
    }

    /**
     * @return interval in millis or 0 if it's not set
     */
    fun getSmsSendInterval(context: Context): Long = obtainPrefs(context)
            .getLong("sms_send_interval", SMS_INTERVAL_DEFAULT_VALUE)

    fun setCoordsFormat(context: Context, format: CoordsFormat) {
        obtainPrefs(context).edit().putString("coords_format", format.name).apply()
    }

    fun getCoordsFormat(context: Context): CoordsFormat {
        return try {
            CoordsFormat.valueOf(
                    obtainPrefs(context).getStringOrDefault(
                            key = "coords_format",
                            default = COORDS_FORMAT_DEFAULT_VALUE.name
                    )
            )
        } catch (e: Exception) {
            obtainPrefs(context).edit().putString("coords_format", COORDS_FORMAT_DEFAULT_VALUE.name).apply()
            COORDS_FORMAT_DEFAULT_VALUE
        }
    }

    fun setMaxSMSCount(context: Context, maxSMSCount: Int) {
        obtainPrefs(context).edit().putInt("max_sms_count", maxSMSCount).apply()
    }

    fun getMaxSMSCount(context: Context): Int = obtainPrefs(context)
            .getInt("max_sms_count", SMS_MAX_COUNT_DEFAULT_VALUE)

    fun putSmsSendEnabled(context: Context, isSMSSendEnabled: Boolean) {
        obtainPrefs(context).edit().putBoolean("sms_send_enabled", isSMSSendEnabled).apply()
    }

    fun isSmsSendEnabled(context: Context): Boolean = obtainPrefs(context).getBoolean("sms_send_enabled", false)

    fun putSmsSendTriggerTime(context: Context, triggerTime: Long) {
        obtainPrefs(context).edit().putLong("sms_send_trigger_time", triggerTime).apply()
    }

    fun getSmsSendTriggerTime(context: Context): Long =
            obtainPrefs(context).getLong("sms_send_trigger_time", 0L)

    fun putLastSmsAccuracy(context: Context, accuracy: Float) {
        obtainPrefs(context).edit().putFloat("last_sms_accuracy", accuracy).apply()
    }

    fun getLastSmsAccuracy(context: Context): Float =
            obtainPrefs(context).getFloat("last_sms_accuracy", 0f)

    fun putWhoCanRequestLocation(context: Context, whoCanRequestLocation: WhoCanRequestLocation) {
        obtainPrefs(context).edit().putString("who_can_request_location", whoCanRequestLocation.name).apply()
    }

    fun whoCanRequestLocation(context: Context): WhoCanRequestLocation {
        val name = obtainPrefs(context).getStringOrDefault(
                key = "who_can_request_location",
                default = WhoCanRequestLocation.BY_CODE_WORD.name
        )
        return try {
            WhoCanRequestLocation.valueOf(name)
        } catch (th: Throwable) {
            WhoCanRequestLocation.BY_CODE_WORD
        }
    }

    fun getActiveSubscriptionId(context: Context): Int =
            obtainPrefs(context).getInt("active_subscription_id", RSTSmsManager.defaultActiveSimId)

    fun setActiveSubscriptionId(context: Context, id: Int) {
        obtainPrefs(context).edit().putInt("active_subscription_id", id).apply()
    }

    private fun obtainPrefs(context: Context): SharedPreferences = context.getSharedPreferences("RSTPreferences", Context.MODE_PRIVATE)
}