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

package ru.rescuesmstracker.onboarding

import android.content.Context
import android.location.Location
import android.os.Build
import android.support.v4.content.ContextCompat
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import ru.rescuesmstracker.RSTSmsManager
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rst.rescuesmstracker.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FormatUtils(val context: Context) {

    fun getSMSCountString(smsCount: Int): String {
        return when (smsCount) {
            RSTPreferences.customSmsCountValue -> context.getString(R.string.format_custom)
            RSTPreferences.SMS_COUNT_INFINITE -> context.getString(R.string.sms_count_infinite)
            else -> context.getString(R.string.sms_count_format, smsCount.toString())
        }
    }

    fun getIntervalFormattedString(intervalMillis: Long): String {
        return if (intervalMillis == RSTPreferences.customIntervalValue) {
            context.getString(R.string.format_custom)
        } else {
            context.getString(R.string.interval_format,
                    (TimeUnit.MILLISECONDS.toMinutes(intervalMillis)).toString())
        }
    }

    fun formatLocationSms(location: Location, isLastKnown: Boolean): String {
        val formattedTime = SimpleDateFormat("kk:mm", Locale.getDefault())
                .format(location.time)
        val timePrefix = if (isLastKnown) "OLD " else ""

        return context.getString(R.string.sms_pattern,
                RSTPreferences.getCoordsFormat(context).format(location),
                "${timePrefix}T=$formattedTime",
                String.format("%.0f", location.accuracy))
    }

    fun formatTimerMinutes(minutesMillis: Long): String =
            if (minutesMillis == 0L) "" else (Math.max(1, minutesMillis / DateUtils.MINUTE_IN_MILLIS)).toString()

    fun isValidPhoneNumber(number: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val countryIso = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simCountryIso
            PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.formatNumberToE164(number,
                    (if (countryIso.isEmpty()) Locale.getDefault().country else Locale.getDefault().country)))
        } else {
            return PhoneNumberUtils.isGlobalPhoneNumber(number.filter { PhoneNumberUtils.isDialable(it) })
        }
    }

    fun formatActiveSim(id: Int): String {
        return context.getString(R.string.sim, id.toString())
    }

    fun formatCarrierName(name: CharSequence?): CharSequence {
        return name ?: ""
    }

    fun formatSimInfo(simInfo: RSTSmsManager.SimInfo): CharSequence {
        val idString = formatActiveSim(simInfo.id)
        val builder = SpannableStringBuilder()
        builder.append(idString).append('\n').append(formatCarrierName(simInfo.carrierName))

        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.white_50_alpha)),
                idString.length, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    fun areEqualPhoneNumbers(lhs: String, rhs: String): Boolean = PhoneNumberUtils.compare(lhs, rhs)
}