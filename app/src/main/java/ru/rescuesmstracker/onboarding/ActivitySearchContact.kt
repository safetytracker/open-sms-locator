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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.a_search_contact.*
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.extensions.drawable
import ru.rescuesmstracker.onboarding.model.OnBoardingContactsProvider
import ru.rescuesmstracker.widget.BaseRSTActivity
import ru.rescuesmstracker.widget.ContactView
import ru.rst.rescuesmstracker.R

class ActivitySearchContact : BaseRSTActivity(), OnBoardingContactsProvider.ContactsCallback {

    companion object {
        const val CONTACT_KEY = "ru.rescuesmstracker.ActivitySearchContact.CONTACT_KEY"
    }

    private val adapter = ContactsAdapter { contact ->
        run {
            setResultContactAndFinish(contact)
        }
    }
    private val contactsProvider = OnBoardingContactsProvider()

    override fun createActivity(savedInstanceState: Bundle?) {
        super.createActivity(savedInstanceState)
        setContentView(R.layout.a_search_contact)

        btnBack.setOnClickListener { onBackPressed() }
        btnAdd.setOnClickListener {
            val contact = Contact()
            contact.phone = inputSearch.text.toString()
            setResultContactAndFinish(contact)
        }
        btnAdd.isEnabled = false
        val listener = object : TextWatcher {
            val formatUtils = FormatUtils(this@ActivitySearchContact)

            override fun afterTextChanged(s: Editable) {
                btnAdd.isEnabled = !s.isEmpty() && formatUtils.isValidPhoneNumber(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                contactsProvider.queryContacts(this@ActivitySearchContact, s.toString(), this@ActivitySearchContact)
            }
        }
        inputSearch.addTextChangedListener(listener)

        listContacts.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(listContacts.context,
                (listContacts.layoutManager as LinearLayoutManager).orientation)
        dividerItemDecoration.setDrawable(drawable(R.drawable.divider_list_contacts))
        listContacts.addItemDecoration(dividerItemDecoration)
        contactsProvider.queryContacts(this, "", this)
    }

    private fun setResultContactAndFinish(contact: Contact) {
        val intent = Intent()
        intent.putExtra(CONTACT_KEY, contact)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onSuccess(query: String, contacts: List<Contact>) {
        adapter.contacts.clear()
        adapter.contacts.addAll(contacts)
        adapter.currentQuery = query
        adapter.notifyDataSetChanged()
    }

    override fun onError(error: Throwable) {
        // do nothing
    }

    class ContactsAdapter(private val onContactSelected: (Contact) -> Unit) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {
        val contacts: MutableList<Contact> = ArrayList()
        var currentQuery: String = ""

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            holder.bind(contacts[position], currentQuery)
            holder.itemView.setOnClickListener { onContactSelected.invoke(contacts[position]) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val contactView = ContactView(parent.context)
            contactView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return ContactViewHolder(contactView)
        }

        override fun getItemCount(): Int = contacts.size

        open class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            @ColorInt
            val color: Int = ContextCompat.getColor(itemView.context, R.color.amber_600)

            fun bind(contact: Contact, query: String) {
                (itemView as ContactView).setContact(contact, query)
            }
        }
    }
}