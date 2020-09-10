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

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.View
import kotlinx.android.synthetic.main.f_base_onboarding.*
import kotlinx.android.synthetic.main.f_permissions.*
import ru.rescuesmstracker.Constants
import ru.rescuesmstracker.extensions.color
import ru.rescuesmstracker.extensions.drawable
import ru.rescuesmstracker.extensions.isPermissionGranted
import ru.rst.rescuesmstracker.R

@TargetApi(Build.VERSION_CODES.M)
class PermissionsFragment : BaseOnBoardingFragment() {

    private enum class State(@ColorRes val titleTextColorRes: Int,
                             val isButtonActivated: Boolean,
                             @StringRes val titleRes: Int,
                             @DrawableRes val splashDrawableRes: Int) {
        IDLE(R.color.white, false, R.string.permissions_title, R.drawable.splash_permissions),
        ERROR(R.color.red_500, true, R.string.permissions_title_error, R.drawable.splash_permissions_error);
    }

    interface PermissionsListener {
        fun onAllPermissionsGranted()
    }

    companion object {
        private val PERMISSIONS = arrayOf("android.permission.RECEIVE_SMS",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_SMS",
                "android.permission.SEND_SMS",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_PHONE_STATE")

        fun hasPermissionsToRequest(context: Context): Boolean {
            return PERMISSIONS.any { !isPermissionGranted(context, it) }
        }

        private fun isPermissionGranted(context: Context, permission: String): Boolean =
                context.isPermissionGranted(permission)
    }

    var permissionsListener: PermissionsListener? = null

    private var state: State = State.IDLE
        set(value) {
            text_title.setTextColor(requireActivity().color(value.titleTextColorRes))
            text_title.setText(value.titleRes)
            go_further_button.isActivated = value.isButtonActivated
            img_splash.setImageDrawable(requireActivity().drawable(value.splashDrawableRes))
            field = value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        go_further_button.setOnClickListener {
            requestPermissions(PERMISSIONS.filter { !isPermissionGranted(requireActivity(), it) }.toTypedArray(),
                    Constants.REQUEST_PERMISSIONS_REQUEST_CODE)
        }

        state = if (savedInstanceState == null) State.IDLE else State.valueOf(savedInstanceState.getString("state")!!)

        if (!hasPermissionsToRequest(requireActivity())) {
            onBoardingController?.goToNextScreen()
            permissionsListener?.onAllPermissionsGranted()
        }
        go_further_button.text = getString(R.string.permissions_permit)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("state", state.name)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                state = State.ERROR
            } else {
                onBoardingController?.goToNextScreen()
                permissionsListener?.onAllPermissionsGranted()
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.f_permissions
}