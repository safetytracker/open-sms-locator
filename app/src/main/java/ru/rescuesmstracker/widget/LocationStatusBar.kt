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

package ru.rescuesmstracker.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import ru.rescuesmstracker.extensions.drawable
import ru.rst.rescuesmstracker.R

class LocationStatusBar : AppCompatTextView {
    enum class Status(@DrawableRes val iconRes: Int,
                      @ColorRes val tintColorRes: Int,
                      @StringRes val textRes: Int,
                      @DrawableRes val backgroundDrawable: Int) {
        ACCURACY_UNKNOWN(R.drawable.ic_location_fine_24dp, R.color.white_70_alpha,
                0, 0),
        ACCURACY_KNOWN(R.drawable.ic_location_fine_24dp, R.color.white_70_alpha,
                R.string.location_status_accuracy, 0),
        LOCATION_DISABLED(R.drawable.ic_location_inabled_24dp, R.color.white,
                R.string.location_status_location_disabled, R.drawable.bg_location_disabled);
    }

    var status: Status? = null
        set(value) {
            if (field != value && value != null) {
                val tintColor = ContextCompat.getColor(context, value.tintColorRes)
                setIcon(value.iconRes, tintColor)
                setTextColor(tintColor)
                setBackgroundResource(value.backgroundDrawable)
                if (value.textRes == 0) {
                    text = ""
                } else {
                    if (value == Status.ACCURACY_KNOWN) {
                        text = context.getString(value.textRes, formattedAccuracy())
                    } else {
                        setText(value.textRes)
                    }
                }
                field = value
            }
        }

    var accuracy: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                text = context.getString(Status.ACCURACY_KNOWN.textRes, formattedAccuracy())
            }
        }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setBackgroundColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.location_status_text_size).toFloat())
        compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.location_status_icon_padding)
        gravity = Gravity.CENTER_VERTICAL

        status = Status.ACCURACY_UNKNOWN

        val padding = resources.getDimensionPixelSize(R.dimen.location_status_sides_padding)
        setPadding(padding, 0, padding, 0)

        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    fun onUpdateStatus(accuracy: Float, isLocationEnabled: Boolean) {
        this.accuracy = accuracy
        status = if (!isLocationEnabled) {
            Status.LOCATION_DISABLED
        } else {
            if (accuracy == 0f) {
                Status.ACCURACY_UNKNOWN
            } else {
                Status.ACCURACY_KNOWN

            }
        }
    }

    private fun formattedAccuracy(): String = String.format("%.2f", accuracy)

    private fun setIcon(@DrawableRes iconRes: Int, @ColorInt color: Int) {
        val icon = context.drawable(iconRes).mutate()
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    }
}