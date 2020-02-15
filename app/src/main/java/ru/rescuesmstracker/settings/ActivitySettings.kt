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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.view.View
import kotlinx.android.synthetic.main.a_settings.*
import kotlinx.android.synthetic.main.v_main_settings.*
import ru.rescuesmstracker.Constants
import ru.rescuesmstracker.RSTSmsManager
import ru.rescuesmstracker.WhoCanRequestLocation
import ru.rescuesmstracker.contacts.ContactsController
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.onboarding.ActivitySearchContact
import ru.rescuesmstracker.onboarding.FormatUtils
import ru.rescuesmstracker.widget.BaseRSTActivity
import ru.rescuesmstracker.widget.dialog.InputDialogMode
import ru.rescuesmstracker.widget.dialog.RSTAlertDialog
import ru.rescuesmstracker.widget.dialog.RadioButtonsDialogMode
import ru.rescuesmstracker.widget.dialog.TextDialogMode
import ru.rst.rescuesmstracker.BuildConfig
import ru.rst.rescuesmstracker.R

class ActivitySettings : BaseRSTActivity() {

    private lateinit var mainSettingsController: MainSettingsController
    private val contactsAdapter = ContactsAdapter()
    private val formatUtils = FormatUtils(this)

    init {
        contactsAdapter.onContactRemoved = {
            val removedPosition = contactsAdapter.removeContact(it)
            contactsAdapter.notifyItemRemoved(removedPosition)
            contactsAdapter.notifyItemChanged(removedPosition, contactsAdapter.itemCount)
            ContactsController.removeContact(it)
        }
    }

    override fun createActivity(savedInstanceState: Bundle?) {
        super.createActivity(savedInstanceState)

        setContentView(R.layout.a_settings)
        mainSettingsController = MainSettingsController(this, pref_interval, pref_max_sms_count, pref_coords_format)
        mainSettingsController.initViews()

        btnBack.setOnClickListener { onBackPressed() }
        contactsAdapter.refillWithContacts(ContactsController.loadAllContacts())
        listContacts.adapter = contactsAdapter
        btnAddPhone.setOnClickListener {
            val intent = Intent(this@ActivitySettings, ActivitySearchContact::class.java)
            startActivityForResult(intent, Constants.PHONE_SELECT_ACTIVITY_REQUEST_CODE)
        }
        btnCodeWordWhy.setOnClickListener {
            val dialog = RSTAlertDialog()
            dialog.dialogMode = TextDialogMode(this@ActivitySettings)
                    .setText(R.string.settings_code_word_why_answer)
            dialog.cancelText = ""
            dialog.setTitle(getString(R.string.settings_code_word_why)).show(supportFragmentManager, "")
        }

        btnProblems.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", BuildConfig.FEEDBACK_EMAIL, null))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_theme))
            startActivity(Intent.createChooser(intent, getString(R.string.feedback_email_title)))
        }

        codeWord.setValue(RSTPreferences.getCodeWord(this))
        codeWord.setOnClickListener {
            val dialog = RSTAlertDialog()
            val dialogMode = InputDialogMode(this@ActivitySettings, codeWord.getValue())
            dialogMode.hint = getString(R.string.code_word_hint)
            dialog.dialogMode = dialogMode
            dialog.onOkListener = View.OnClickListener {
                val newCodeWord = dialogMode.getInput()
                codeWord.setValue(newCodeWord)
                RSTPreferences.putCodeWord(this, newCodeWord)

                if (newCodeWord.isEmpty()) {
                    dialog.dismiss()
                    return@OnClickListener
                }

                dialog.okText = getString(R.string.send_now)
                dialog.dialogMode = null
                dialog.onOkListener = View.OnClickListener {
                    for (contact in ContactsController.loadAllContacts()) {
                        RSTSmsManager.get().sendTextMessage(this, contact.phone,
                                null, RSTPreferences.getCodeWord(this), null, null)
                    }
                    dialog.dismiss()
                }
                dialog.setTitle(getString(R.string.code_word_changed_send))
                dialog.cancelText = getString(R.string.not_now)
            }
            dialog.dismissOnOk = false
            dialog.setTitle(getString(R.string.code_word_dialog_title))
                    .show(supportFragmentManager, "")
        }

        prefWhoCanRequestLocation.setValue(getString(RSTPreferences.whoCanReqeustLocation(this).textRes))
        val whoCanRequestLocationDialogMode = RadioButtonsDialogMode<WhoCanRequestLocation>()
        whoCanRequestLocationDialogMode.addButtons(WhoCanRequestLocation.values())
        whoCanRequestLocationDialogMode.textProvider = { value -> getString(value.textRes) }
        prefWhoCanRequestLocation.setOnClickListener {
            whoCanRequestLocationDialogMode.setCheckedId(WhoCanRequestLocation.values().indexOf(RSTPreferences.whoCanReqeustLocation(this)))
            val dialog = RSTAlertDialog()
            dialog.dialogMode = whoCanRequestLocationDialogMode
            dialog.onOkListener = View.OnClickListener {
                if (whoCanRequestLocationDialogMode.getCheckedId() >= 0) {
                    val index = whoCanRequestLocationDialogMode.getCheckedId()
                    RSTPreferences.putWhoCanRequestLocation(this, WhoCanRequestLocation.values()[index])
                    prefWhoCanRequestLocation.setValue(getString(WhoCanRequestLocation.values()[index].textRes))
                }
            }
            dialog.setTitle(prefWhoCanRequestLocation.getTitle())
            dialog.show(supportFragmentManager, "who_can_request_location")
        }

        if (RSTSmsManager.get().canSelectSubscription(this)) {
            val allAvailableSim = RSTSmsManager.get().getAllAvailableSimIds(this)
            prefActiveSim.visibility = View.VISIBLE
            prefActiveSim.setValue(formatUtils.formatCarrierName(RSTSmsManager.get()
                    .getActiveSimId(this).carrierName))
            val activeSimDialogMode = RadioButtonsDialogMode<RSTSmsManager.SimInfo>()
            activeSimDialogMode.addButtons(allAvailableSim)
            activeSimDialogMode.textProvider = { value -> formatUtils.formatSimInfo(value) }
            prefActiveSim.setOnClickListener {
                activeSimDialogMode.setCheckedId(allAvailableSim.indexOf(RSTSmsManager.get().getActiveSimId(this)))
                val dialog = RSTAlertDialog()
                dialog.dialogMode = activeSimDialogMode
                dialog.onOkListener = View.OnClickListener {
                    if (activeSimDialogMode.getCheckedId() >= 0) {
                        val index = activeSimDialogMode.getCheckedId()
                        val result = allAvailableSim[index]
                        RSTPreferences.setActiveSubscriptionId(this, result.id)
                        prefActiveSim.setValue(formatUtils.formatCarrierName(RSTSmsManager.get()
                                .getActiveSimId(this).carrierName))
                    }
                }
                dialog.setTitle(prefActiveSim.getTitle())
                dialog.show(supportFragmentManager, "active_sms")
            }
        } else {
            prefActiveSim.visibility = View.GONE
        }

        textVersion.text = getString(R.string.settings_version,
                packageManager.getPackageInfo(packageName, 0).versionName)

        btnShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://safetytracker.org")
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.settings_share)))
        }

        canScroll {
            if (it) {
                settingsScrollView.setOnScrollChangeListener(
                        NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY
                            ->
                            if (scrollY == 0) {
                                btnScrollBottom.show()
                            } else {
                                btnScrollBottom.hide()
                            }
                        })
                btnScrollBottom.visibility = View.VISIBLE
                btnScrollBottom.setOnClickListener {
                    settingsScrollView.smoothScrollBy(0,
                            resources.getDimensionPixelSize(R.dimen.settings_button_scroll_by))
                }
            } else {
                btnScrollBottom.visibility = View.GONE
            }
        }

    }

    private fun canScroll(onResult: (Boolean) -> Unit) {
        settingsScrollView.post {
            val child = settingsScrollView.getChildAt(0)
            if (child != null) {
                val childHeight = child.height
                onResult.invoke(settingsScrollView.height < childHeight +
                        settingsScrollView.paddingTop + settingsScrollView.paddingBottom)
            } else {
                onResult.invoke(false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PHONE_SELECT_ACTIVITY_REQUEST_CODE) {
            val receivedContact: Contact? = data?.getParcelableExtra(ActivitySearchContact.CONTACT_KEY)
            if (receivedContact != null) {
                val savedContact = ContactsController.addContact(receivedContact)
                contactsAdapter.addContact(savedContact)
                contactsAdapter.notifyItemInserted(contactsAdapter.itemCount - 1)

                val dialog = RSTAlertDialog()
                val dialogMode = TextDialogMode(this)
                dialog.dialogMode = dialogMode
                dialog.okText = getString(R.string.send_now)
                dialog.onOkListener = View.OnClickListener {
                    RSTSmsManager.get().sendTextMessage(this, savedContact.phone,
                            null, RSTPreferences.getCodeWord(this), null, null)
                }
                dialog.setTitle(getString(R.string.trusted_phone_added_send_code_title))
                dialog.cancelText = getString(R.string.not_now)
                dialog.show(supportFragmentManager, "send_code_word_to_all")
            }
        }
    }
}