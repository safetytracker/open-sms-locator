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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.widget.ContactView

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    var onContactRemoved: ((Contact) -> Unit)? = null
    private var contacts: MutableList<Contact> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.itemView as ContactView).setContact(contacts[position]).
                onRemoveListener = View.OnClickListener {
            if (position < contacts.size) {
                onContactRemoved?.invoke(contacts[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val contactView = ContactView(parent.context)
        contactView.isRemovingEnabled = true
        contactView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(contactView)
    }

    override fun getItemCount(): Int = contacts.size

    fun addContact(contact: Contact) {
        contacts.add(contact)
    }

    fun refillWithContacts(contacts: List<Contact>) {
        this.contacts = contacts.toMutableList()
    }

    fun removeContact(contact: Contact): Int {
        val position = contacts.indexOf(contact)
        contacts.remove(contact)
        return position
    }

    class ViewHolder(contactView: ContactView) : RecyclerView.ViewHolder(contactView)
}