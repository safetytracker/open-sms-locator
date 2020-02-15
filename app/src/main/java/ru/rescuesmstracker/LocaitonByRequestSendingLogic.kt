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

import android.content.Context
import android.support.annotation.StringRes
import android.telephony.SmsMessage
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.onboarding.FormatUtils
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rst.rescuesmstracker.R

enum class WhoCanRequestLocation(val validator: Validator,
                                 @StringRes val textRes: Int) {
    BY_CODE_WORD(ByCodeWord(), R.string.settings_send_location_by_codeword),
    TRUSTED_NUMBERS(TrustedNumbers(), R.string.settings_send_location_to_anyone)
}

abstract class Validator {
    abstract fun getAllSmsDestinations(context: Context, smsMessages: Array<SmsMessage?>, trustedContacts: List<Contact>): List<Contact>
}

open class ByCodeWord : Validator() {
    override fun getAllSmsDestinations(context: Context, smsMessages: Array<SmsMessage?>, trustedContacts: List<Contact>): List<Contact> {
        return smsMessages.filterNotNull().filter {
            val currentCodeWord = RSTPreferences.getCodeWord(context)
            if (currentCodeWord.isEmpty()) {
                false
            } else {
                it.messageBody == currentCodeWord
            }
        }.map { smsMessage ->
            trustedContacts.find {
                FormatUtils(context).areEqualPhoneNumbers(
                        it.phone, smsMessage.originatingAddress)
            } ?: Contact("code_word_contact", smsMessage.originatingAddress, "", false)
        }
    }
}

class TrustedNumbers : ByCodeWord() {
    override fun getAllSmsDestinations(context: Context, smsMessages: Array<SmsMessage?>, trustedContacts: List<Contact>): List<Contact> {
        return super.getAllSmsDestinations(context, smsMessages, trustedContacts).filter { codeWordContact ->
            trustedContacts.any {
                FormatUtils(context).areEqualPhoneNumbers(
                        it.phone, codeWordContact.phone)
            }
        }
    }
}