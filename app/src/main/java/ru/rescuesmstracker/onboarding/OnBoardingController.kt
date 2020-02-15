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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import ru.rescuesmstracker.onboarding.fragments.*
import ru.rescuesmstracker.settings.RSTPreferences
import ru.rescuesmstracker.RSTSmsManager
import ru.rescuesmstracker.contacts.ContactsController
import ru.rescuesmstracker.data.Contact

open class OnBoardingController(val view: IOnBoardingView, val context: Context) {

    enum class OnBoardingScreen(private val fragmentClass: String) {
        WELCOME(WelcomeFragment::class.java.name),
        PERMISSIONS(PermissionsFragment::class.java.name),
        TRUSTED_PHONE_NUMBERS(TrustedPhoneNumberFragment::class.java.name),
        CODE_WORD(CodeWordFragment::class.java.name),
        SETTINGS(OnBoardingSettingsFragment::class.java.name);

        fun createScreen(context: Context, args: Bundle?): BaseOnBoardingFragment =
                Fragment.instantiate(context, fragmentClass, args) as BaseOnBoardingFragment
    }

    private val prefs = getPrefs(context)
    private var contact: Contact? = null
    private lateinit var currentScreen: OnBoardingScreen

    fun onViewResumed() {
        if (getPrefs(context).getBoolean("isOnboardingPassed", false)) {
            currentScreen = OnBoardingScreen.SETTINGS
            view.finishView()
        } else {
            currentScreen = OnBoardingScreen.valueOf(prefs.getString("currentScreen", OnBoardingScreen.WELCOME.name))
            if (view.getCurrentScreen() != currentScreen) {
                view.showScreen(currentScreen)
            }
        }
    }

    fun onViewPaused() {
        prefs.edit().putString("currentScreen", currentScreen.name).apply()
    }

    fun goToNextScreen() {
        val nextScreenOrdinal: Int = currentScreen.ordinal + 1
        val possibleScreens: Array<OnBoardingScreen>
                = OnBoardingScreen.values()
        if (nextScreenOrdinal < possibleScreens.size) {
            currentScreen = possibleScreens[nextScreenOrdinal]
            view.showScreen(currentScreen)
        } else {
            prefs.edit().putBoolean("isOnboardingPassed", true).apply()
            view.finishView()
        }
    }

    fun onContactSelected(contact: Contact) {
        ContactsController.addContact(contact)
    }

    fun onCodeWordSet(codeWord: String) {
        RSTPreferences.putCodeWord(context, codeWord)
    }

    fun sendCodeWord(contact: Contact, word: String) {
        RSTSmsManager.get().sendTextMessage(context, contact.phone, null, word, null, null)
    }

    fun getContact(): Contact {
        if (contact == null) {
            contact = ContactsController.loadTopContact()
        }
        return contact!!
    }

    companion object {
        private fun getPrefs(context: Context): SharedPreferences = context
                .getSharedPreferences("OnBoardingController", Context.MODE_PRIVATE)
    }
}