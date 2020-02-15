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
import android.util.AttributeSet
import android.widget.ImageView
import ru.rst.rescuesmstracker.R

class PlayButton : ImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var playing = false
        set(value) {
            if (value) {
                setImageResource(R.drawable.ic_pause)
            } else {
                setImageResource(R.drawable.ic_play)
            }
            field = value
        }

    init {
        playing = false
        scaleType = ScaleType.CENTER
        setBackgroundResource(R.drawable.circle_button_bg)
    }
}