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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.rescuesmstracker.Constants
import ru.rescuesmstracker.PermissionsActivity
import ru.rescuesmstracker.onboarding.OnBoardingActivity

open class BaseRSTActivity : AppCompatActivity() {

    protected var isCreated = false
    private var savedInstanceState: Bundle? = null

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this is PermissionsActivity
                || this is OnBoardingActivity
                || !PermissionsActivity.checkPermissions(this)) {
            createActivity(savedInstanceState)
        } else {
            this.savedInstanceState = savedInstanceState
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.ACTIVITY_PERMISSIONS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (!isCreated) {
                createActivity(savedInstanceState)
            }
            this.savedInstanceState = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    protected open fun createActivity(savedInstanceState: Bundle?) {
        isCreated = true
    }
}