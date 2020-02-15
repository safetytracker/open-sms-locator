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

package ru.rescuesmstracker.utils

import android.content.Context
import android.content.SharedPreferences

object AppUpdateManager {

    interface AppVersionListener {
        fun onNew()
        fun onUpdate(prevVersionCode: Int, currentVersionCode: Int)
        fun onSame()
    }

    private val versionCodeKey = "ru.rescuesmstracker.utils.AppUpdateManager.versionCodeKey"

    fun check(context: Context, listener: AppVersionListener?) {
        val prefs = obtainPrefs(context)
        val prevVersion = prefs.getInt(versionCodeKey, -1)
        val currentVersionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        if (prevVersion == -1) {
            listener?.onNew()
        } else {
            if (currentVersionCode == prevVersion) {
                listener?.onSame()
            } else if (currentVersionCode > prevVersion) {
                listener?.onUpdate(prevVersion, currentVersionCode)
            }
        }
        prefs.edit().putInt(versionCodeKey, currentVersionCode).apply()
    }

    private fun obtainPrefs(context: Context): SharedPreferences
            = context.getSharedPreferences("AppUpdateManagerPrefs", Context.MODE_PRIVATE)
}