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

package ru.rescuesmstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import ru.rescuesmstracker.onboarding.fragments.PermissionsFragment
import ru.rescuesmstracker.widget.BaseRSTActivity
import ru.rst.rescuesmstracker.R

class PermissionsActivity : BaseRSTActivity() {

    override fun createActivity(savedInstanceState: Bundle?) {
        super.createActivity(savedInstanceState)
        setContentView(R.layout.a_permissions)

        val fragment = PermissionsFragment()
        fragment.permissionsListener = object : PermissionsFragment.PermissionsListener {
            override fun onAllPermissionsGranted() {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }

    companion object {
        fun checkPermissions(context: Context): Boolean {
            return if (PermissionsFragment.hasPermissionsToRequest(context)) {
                context.startActivity(intent(context))
                true
            } else {
                false
            }
        }

        fun checkPermissions(activity: Activity): Boolean {
            return if (PermissionsFragment.hasPermissionsToRequest(activity)) {
                activity.startActivityForResult(intent(activity), Constants.ACTIVITY_PERMISSIONS_REQUEST_CODE)
                true
            } else {
                false
            }
        }

        fun intent(context: Context): Intent = Intent(context, PermissionsActivity::class.java)
    }
}