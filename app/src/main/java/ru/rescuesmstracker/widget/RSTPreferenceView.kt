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
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.v_rst_preference.view.*
import ru.rst.rescuesmstracker.R

class RSTPreferenceView : LinearLayout {

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.v_rst_preference, this)

        val topBotPadding = resources.getDimensionPixelSize(R.dimen.rst_preference_topbot_padding)
        setPadding(0, topBotPadding, 0, topBotPadding)
        orientation = VERTICAL

        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.RSTPreferenceView)
            textRSTPreferenceValue.text = array.getString(R.styleable.RSTPreferenceView_value)
            textRSTPreferenceTitle.text = array.getString(R.styleable.RSTPreferenceView_title)
            array.recycle()
        }
    }

    fun setValue(text: CharSequence) {
        textRSTPreferenceValue.text = text
    }

    fun getValue(): CharSequence = textRSTPreferenceValue.text

    fun getTitle(): String = textRSTPreferenceTitle.text.toString()
}