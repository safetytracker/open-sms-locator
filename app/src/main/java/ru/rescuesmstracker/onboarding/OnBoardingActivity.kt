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

package ru.rescuesmstracker.onboarding

import android.content.Intent
import android.os.Bundle
import ru.rescuesmstracker.timer.ActivityTimer2
import ru.rescuesmstracker.widget.BaseRSTActivity
import ru.rst.rescuesmstracker.R

class OnBoardingActivity : BaseRSTActivity(), IOnBoardingView {

    private lateinit var controller: OnBoardingController
    private var screen: OnBoardingController.OnBoardingScreen? = null

    override fun createActivity(savedInstanceState: Bundle?) {
        super.createActivity(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val screenName = savedInstanceState?.getString("screen")
        if (screenName != null) {
            screen = OnBoardingController.OnBoardingScreen.valueOf(screenName)
        }
        controller = OnBoardingController(this, this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (screen != null) {
            outState?.putString("screen", screen!!.name)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCreated) {
            controller.onViewResumed()
        }
    }

    override fun onPause() {
        if (isCreated) {
            controller.onViewPaused()
        }
        super.onPause()
    }

    override fun showScreen(screen: OnBoardingController.OnBoardingScreen) {
        val fragment = screen.createScreen(this, null)
        fragment.onBoardingController = controller
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment, screen.name)
                .commitAllowingStateLoss()
        this.screen = screen
    }

    override fun getCurrentScreen(): OnBoardingController.OnBoardingScreen? {
        return screen
    }

    override fun finishView() {
        startActivity(Intent(this, ActivityTimer2::class.java))
        finish()
    }
}
