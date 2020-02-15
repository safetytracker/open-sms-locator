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

package ru.rescuesmstracker.settings

import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.InputType
import android.view.View
import ru.rescuesmstracker.onboarding.FormatUtils
import ru.rescuesmstracker.utils.TextWatcherAdapter
import ru.rescuesmstracker.widget.RSTPreferenceView
import ru.rescuesmstracker.widget.dialog.InputDialogMode
import ru.rescuesmstracker.widget.dialog.RSTAlertDialog
import ru.rescuesmstracker.widget.dialog.RadioButtonsDialogMode
import ru.rst.rescuesmstracker.R
import java.util.concurrent.TimeUnit

class MainSettingsController(
        private val activity: FragmentActivity,
        private val pref_interval: RSTPreferenceView,
        private val pref_max_sms_count: RSTPreferenceView,
        private val pref_coords_format: RSTPreferenceView) {

    private var intervalValues: Array<Long> = arrayOf(
            TimeUnit.MINUTES.toMillis(10),
            RSTPreferences.SMS_INTERVAL_DEFAULT_VALUE,
            TimeUnit.HOURS.toMillis(1),
            RSTPreferences.customIntervalValue)
    private var maxSMSCountValues: Array<Int> = arrayOf(
            RSTPreferences.SMS_MAX_COUNT_DEFAULT_VALUE,
            200,
            500,
            RSTPreferences.customSmsCountValue)
    private var coordsFormatValues: Array<RSTPreferences.CoordsFormat> = RSTPreferences.CoordsFormat.values()
    private lateinit var formatUtils: FormatUtils

    fun initViews() {
        formatUtils = FormatUtils(activity)

        pref_interval.setValue(formatUtils.getIntervalFormattedString(RSTPreferences.getSmsSendInterval(activity)))
        val intervalDialogMode = RadioButtonsDialogMode<Long>()
                .addButtons(intervalValues)
        intervalDialogMode.textProvider = { value -> formatUtils.getIntervalFormattedString(value) }
        pref_interval.setOnClickListener {
            var initialIndex = intervalValues.indexOf(RSTPreferences.getSmsSendInterval(activity))
            initialIndex = if (initialIndex == -1) intervalValues.indexOf(RSTPreferences.customIntervalValue) else initialIndex
            intervalDialogMode.setCheckedId(initialIndex)

            val dialog = showDialog("pref_interval", pref_interval.getTitle(), intervalDialogMode) { dialog, index ->
                val value = if (intervalValues[index] == RSTPreferences.customIntervalValue) {
                    if (dialog.dialogMode is InputDialogMode) {
                        TimeUnit.MINUTES.toMillis((dialog.dialogMode as InputDialogMode).getInput().toLong())
                    } else {
                        return@showDialog
                    }
                } else {
                    intervalValues[index]
                }
                RSTPreferences.setSMSSendInterval(activity, value)
                pref_interval.setValue(formatUtils.getIntervalFormattedString(value))
                intervalDialogMode.setCheckedId(index)
            }

            intervalDialogMode.onClickedId = { checkedValue ->
                if (checkedValue == RSTPreferences.customIntervalValue) {
                    setNumberInputDialogMode(dialog,
                            activity.getString(R.string.settings_interval_dialog_input_mode)
                    ) {
                        try {
                            it.toInt() > 0
                        } catch (e: Exception) {
                            true
                        }
                    }
                }
            }
        }

        pref_max_sms_count.setValue(formatUtils.getSMSCountString(RSTPreferences.getMaxSMSCount(activity)))
        val smsCountDialogMode = RadioButtonsDialogMode<Int>()
                .addButtons(maxSMSCountValues)
        smsCountDialogMode.textProvider = { value -> formatUtils.getSMSCountString(value) }
        pref_max_sms_count.setOnClickListener {
            var initialIndex = maxSMSCountValues.indexOf(RSTPreferences.getMaxSMSCount(activity))
            initialIndex = if (initialIndex == -1) maxSMSCountValues.indexOf(RSTPreferences.customSmsCountValue) else initialIndex
            smsCountDialogMode.setCheckedId(initialIndex)

            val dialog = showDialog("sms_count", pref_max_sms_count.getTitle(), smsCountDialogMode) { dialog, index ->
                val value = if (maxSMSCountValues[index] == RSTPreferences.customSmsCountValue) {
                    if (dialog.dialogMode is InputDialogMode) {
                        (dialog.dialogMode as InputDialogMode).getInput().toInt()
                    } else {
                        return@showDialog
                    }
                } else {
                    maxSMSCountValues[index]
                }
                RSTPreferences.setMaxSMSCount(activity, value)
                pref_max_sms_count.setValue(formatUtils.getSMSCountString(value))
            }
            smsCountDialogMode.onClickedId = { checkedValue ->
                if (checkedValue == RSTPreferences.customSmsCountValue) {
                    setNumberInputDialogMode(dialog, "")
                }
            }
        }

        pref_coords_format.setValue(activity.getString(RSTPreferences.getCoordsFormat(activity).exampleRes))
        val coordsFormatDialogMode = RadioButtonsDialogMode<RSTPreferences.CoordsFormat>()
        coordsFormatDialogMode.addButtons(coordsFormatValues)
        coordsFormatDialogMode.textProvider = { value -> activity.getString(value.exampleRes) }
        pref_coords_format.setOnClickListener {
            coordsFormatDialogMode.setCheckedId(coordsFormatValues.indexOf(RSTPreferences.getCoordsFormat(activity)))
            showDialog("coords_format", pref_coords_format.getTitle(), coordsFormatDialogMode) { _, index ->
                RSTPreferences.setCoordsFormat(activity, coordsFormatValues[index])
                pref_coords_format.setValue(activity.getString(coordsFormatValues[index].exampleRes))
            }
        }
    }

    private fun setNumberInputDialogMode(dialog: RSTAlertDialog, hint: CharSequence, okFilter: ((String) -> Boolean)? = null) {
        val dialogMode = InputDialogMode(activity)
        dialogMode.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        dialogMode.filter = { character -> Character.isDigit(character) }
        dialogMode.hint = hint
        dialogMode.textListener = object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                dialog.okEnabled = !s.isNullOrEmpty()
                        && isInt(s.toString())
                        && okFilter?.invoke(s.toString()) ?: true
            }

            fun isInt(string: String): Boolean = string.toShortOrNull() != null
        }
        dialog.okEnabled = !dialogMode.getInput().isEmpty()
        dialog.dialogMode = dialogMode
    }

    private fun <T> showDialog(tag: String, title: String,
                               dialogMode: RadioButtonsDialogMode<T>,
                               onOkListener: (RSTAlertDialog, Int) -> Unit): RSTAlertDialog {
        val dialog = RSTAlertDialog()
        dialog.dialogMode = dialogMode
        dialog.onOkListener = View.OnClickListener {
            if (dialogMode.getCheckedId() >= 0) {
                onOkListener.invoke(dialog, dialogMode.getCheckedId())
            }
        }
        dialog.setTitle(title)
        dialog.show(activity.supportFragmentManager, tag)
        return dialog
    }
}