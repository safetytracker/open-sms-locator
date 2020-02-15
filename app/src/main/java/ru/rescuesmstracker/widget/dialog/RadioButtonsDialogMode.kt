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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import ru.rst.rescuesmstracker.R
import java.util.*

class RadioButtonsDialogMode<ITEM_TYPE> : RSTAlertDialog.DialogMode {

    var textProvider: ((ITEM_TYPE) -> CharSequence) = { it -> it.toString() }
    var onCheckedIdChanged: ((ITEM_TYPE) -> Unit)? = null
    var onClickedId: ((ITEM_TYPE) -> Unit)? = null

    private val buttons = LinkedHashMap<Int, ITEM_TYPE>()
    private var radioGroup: RadioGroup? = null
    private var id: Int = 0

    override fun inflateView(container: ViewGroup) {
        val radioGroup = RadioGroup(container.context)
        radioGroup.orientation = LinearLayout.VERTICAL

        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        radioGroup.layoutParams = params

        for ((id, value) in buttons) {
            val radioButton = LayoutInflater.from(container.context)
                    .inflate(R.layout.v_radio_button, radioGroup, false) as RadioButton
            radioButton.id = id
            radioButton.text = textProvider.invoke(value)
            radioButton.setOnClickListener { onClickedId?.invoke(buttons[id]!!) }
            radioGroup.addView(radioButton)
        }
        radioGroup.check(id)
        radioGroup.setOnCheckedChangeListener { _, id ->
            onCheckedIdChanged?.invoke(buttons[id]!!)
            this@RadioButtonsDialogMode.id = id
        }
        container.addView(radioGroup)
        this.radioGroup = radioGroup
    }

    fun addButton(id: Int, item: ITEM_TYPE): RadioButtonsDialogMode<ITEM_TYPE> {
        buttons[id] = item
        return this
    }

    /**
     * Adding buttons with id = index of a corresponding element in array
     */
    fun addButtons(items: Array<ITEM_TYPE>): RadioButtonsDialogMode<ITEM_TYPE> {
        items.forEachIndexed { pos, text -> addButton(pos, text) }
        return this
    }

    /**
     * Adding buttons with id = index of a corresponding element in array
     */
    fun addButtons(items: List<ITEM_TYPE>): RadioButtonsDialogMode<ITEM_TYPE> {
        items.forEachIndexed { pos, text -> addButton(pos, text) }
        return this
    }

    fun getCheckedId(): Int = if (radioGroup == null) 0 else radioGroup!!.checkedRadioButtonId

    fun setCheckedId(id: Int) {
        radioGroup?.check(id)
        this.id = id
    }
}