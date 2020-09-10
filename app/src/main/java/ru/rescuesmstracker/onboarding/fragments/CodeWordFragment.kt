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

package ru.rescuesmstracker.onboarding.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.f_base_onboarding.*
import kotlinx.android.synthetic.main.f_code_word.*
import ru.rescuesmstracker.utils.hideKeyboard
import ru.rst.rescuesmstracker.R

class CodeWordFragment : BaseOnBoardingFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                btnSentCodeWord.isEnabled = !s.isEmpty()
                btnRemoveCodeWord.isEnabled = !s.isEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        inputCodeWord.addTextChangedListener(listener)
        inputCodeWord.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(inputCodeWord)
                true
            } else {
                false
            }
        }

        btnSentCodeWord.setOnClickListener {
            onBoardingController?.sendCodeWord(onBoardingController!!.getContact(),
                    inputCodeWord.text.toString())
        }
        go_further_button.setOnClickListener {
            onBoardingController?.onCodeWordSet(inputCodeWord.text.toString())
            onBoardingController?.goToNextScreen()
        }
        btnRemoveCodeWord.setOnClickListener {
            inputCodeWord.text = null
        }
    }

    override fun getLayoutRes(): Int = R.layout.f_code_word
}