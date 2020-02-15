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

package ru.rescuesmstracker.widget.dialog

import android.content.Context
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import ru.rst.rescuesmstracker.R

class TextDialogMode(context: Context) : RSTAlertDialog.DialogMode {

    private val textView: TextView = LayoutInflater.from(context)
            .inflate(R.layout.v_text_dialog_mode, null, false) as TextView

    override fun inflateView(container: ViewGroup) {
        textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.text
        container.addView(textView)
    }

    fun setText(text: CharSequence): TextDialogMode {
        textView.text = text
        return this
    }

    fun setText(@StringRes textResId: Int): TextDialogMode {
        textView.setText(textResId)
        return this
    }
}