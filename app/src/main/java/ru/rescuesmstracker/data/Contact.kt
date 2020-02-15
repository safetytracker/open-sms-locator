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

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Contact(@PrimaryKey var id: String,
                   var name: String,
                   var phone: String,
                   private var photoUriString: String,
                   var isFromPhoneContacts: Boolean)
    : RealmObject(), Parcelable {

    fun getPhotoUri(): Uri? = if (photoUriString.isBlank()) null
    else try {
        Uri.parse(photoUriString)
    } catch (e: Exception) {
        null
    }

    fun setPhotoUri(uri: Uri) {
        this.photoUriString = uri.toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(photoUriString)
        parcel.writeByte(if (isFromPhoneContacts) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact = Contact(parcel)

        override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)
    }

    constructor(name: String,
                phone: String,
                photoUriString: String,
                isFromPhoneContacts: Boolean) : this(UUID.randomUUID().toString(), name, phone, photoUriString, isFromPhoneContacts)

    constructor() : this("", "", "", false)

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (phone != other.phone) return false
        if (photoUriString != other.photoUriString) return false
        if (isFromPhoneContacts != other.isFromPhoneContacts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + photoUriString.hashCode()
        result = 31 * result + isFromPhoneContacts.hashCode()
        return result
    }
}