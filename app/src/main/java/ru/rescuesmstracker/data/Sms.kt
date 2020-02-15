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

package ru.rescuesmstracker.data

import android.app.Activity
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Sms(@PrimaryKey open var id: String,
               open var destinationContact: Contact?,
               open var scheduledTimestamp: Long,
               open var sendingAttemptTimestamp: Long,
               open var sentTimestamp: Long,
               open var text: String,
               private var status: String,
               private var type: String) : RealmObject() {
    enum class Status {
        SENT, SENDING, FAILED_TO_SEND;

        companion object {
            fun fromResultCode(resultCode: Int): Status
                    = when (resultCode) {
                Activity.RESULT_OK -> SENT
                else -> FAILED_TO_SEND
            }
        }
    }

    enum class Type {
        LOCATION_SCHEDULED, LOCATION_FORCE, LOCATION_FOR_CODE_WORD, CODE_WORD
    }

    fun getStatus(): Status = Status.valueOf(status)

    fun setStatus(status: Status) {
        this.status = status.name
    }

    fun getType(): Type = Type.valueOf(type)

    fun setType(type: Type) {
        this.type = type.name
    }

    /**
     * Creating SMS prepared for sending
     */
    constructor(destinationContact: Contact, scheduledTimestamp: Long, text: String, type: Type)
            : this(UUID.randomUUID().toString(), destinationContact, scheduledTimestamp,
            0, 0, text, Status.SENDING.name, type.name)

    constructor() : this(UUID.randomUUID().toString(), null, 0, 0,
            0, "", Status.SENDING.name, Type.CODE_WORD.name)


}