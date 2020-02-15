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
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import ru.rst.rescuesmstracker.R

class InputDialogMode : RSTAlertDialog.DialogMode {
    constructor(context: Context) : this(context, "")
    constructor(context: Context, initialInput: CharSequence) {
        this.context = context
        this.initialInput = initialInput
    }

    private val initialInput: CharSequence
    private val context: Context
    private lateinit var inputTextLayout: TextInputLayout
    private lateinit var inputText: TextInputEditText
    private var isViewInflated = false
    var inputType: Int = EditorInfo.TYPE_CLASS_TEXT
        set(value) {
            if (isViewInflated) {
                inputText.inputType = value
            }
            field = value
        }
    var hint: CharSequence = ""
        set(value) {
            field = value
            if (isViewInflated) {
                updateHint()
            }
        }
    var filter: ((Char) -> Boolean)? = null
    var textListener: TextWatcher? = null

    override fun inflateView(container: ViewGroup) {
        LayoutInflater.from(container.context)
                .inflate(R.layout.v_input_dialog_mode, container, true)
        inputText = container.findViewById<TextInputEditText>(R.id.inputText) as TextInputEditText
        inputTextLayout = container.findViewById<TextInputLayout>(R.id.inputTextLayout) as TextInputLayout
        inputText.setRawInputType(inputType)
        updateHint()
        inputText.setText(initialInput)
        inputText.addTextChangedListener(object : TextWatcher {
            var filtered: CharSequence = ""
            override fun afterTextChanged(p0: Editable) {
                textListener?.afterTextChanged(p0)
                filtered = if (filter == null) {
                    p0
                } else {
                    p0.filter(filter!!)
                }
                if (!TextUtils.equals(filtered, p0)) {
                    val selectionStart = inputText.selectionStart - 1
                    val selectionEnd = inputText.selectionEnd - 1
                    inputText.setText(filtered)
                    if (selectionStart == selectionEnd) {
                        if (selectionStart > 0) {
                            inputText.setSelection(selectionStart)
                        }
                    } else {
                        if (selectionEnd > 0 && selectionStart > 0) {
                            inputText.setSelection(selectionStart, selectionEnd)
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textListener?.beforeTextChanged(p0, p1, p2, p3)
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textListener?.onTextChanged(p0, p1, p2, p3)
            }
        })

        isViewInflated = true

        inputText.post {
            inputText.requestFocus()
            showKeyboard(inputText)
        }
    }

    private fun showKeyboard(input: EditText) {
        val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(input, 0)
    }

    /**
     * Updates views with value from [hint] field
     */
    private fun updateHint() {
        inputTextLayout.hint = hint
    }

    fun getInput(): String = if (isViewInflated) {
        inputText.text.toString()
    } else {
        initialInput.toString()
    }
}