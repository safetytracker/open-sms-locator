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

package ru.rescuesmstracker.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

fun Context.drawable(@DrawableRes id: Int): Drawable = ContextCompat.getDrawable(this, id)!!

fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.getLocationManager(): LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager