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

package ru.rescuesmstracker.onboarding.model

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.provider.ContactsContract
import android.support.v4.content.ContentResolverCompat
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.rescuesmstracker.PermissionsActivity
import ru.rescuesmstracker.data.Contact

class OnBoardingContactsProvider {

    interface ContactsCallback {
        fun onSuccess(query: String, contacts: List<Contact>)
        fun onError(error: Throwable)
    }

    private val TAG = "ContactsProvider"
    @SuppressLint("InlinedApi")
    private val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
    @SuppressLint("InlinedApi")
    private val SELECTION = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?"

    fun queryContacts(activity: Activity, query: String, callback: ContactsCallback): Disposable? {
        if (!PermissionsActivity.checkPermissions(activity)) {
            return Observable
                    .create<Cursor> { e ->
                        run {
                            if (query.isEmpty()) {
                                e.onNext(ContentResolverCompat.query(activity.contentResolver,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        PROJECTION,
                                        null, null,
                                        null, null))
                            } else {
                                e.onNext(ContentResolverCompat.query(activity.contentResolver,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        PROJECTION,
                                        SELECTION, arrayOf("%$query%"),
                                        null, null))
                            }
                        }
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<List<Contact>> { cursor: Cursor ->
                        val result: MutableList<Contact> = ArrayList(cursor.count)
                        while (cursor.moveToNext()) {
                            result.add(fromCursor(cursor))
                        }
                        result
                    }
                    .subscribe({ callback.onSuccess(query, it) }, {
                        Log.e(TAG, "Failed to load contacts", it)
                        callback.onError(it)
                    })
        } else {
            return null
        }
    }

    private fun fromCursor(cursor: Cursor): Contact {
        val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
        return Contact(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                photoUri ?: "", true)
    }
}
