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

import android.content.Context
import android.provider.MediaStore
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.v_contact.view.*
import ru.rescuesmstracker.data.Contact
import ru.rescuesmstracker.extensions.color
import ru.rst.rescuesmstracker.R
import java.io.FileNotFoundException

class ContactView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var onRemoveListener: OnClickListener? = null
    var isRemovingEnabled = false
        set(value) {
            field = value
            btnRemove.visibility = if (value) View.VISIBLE else View.GONE
        }

    init {
        View.inflate(context, R.layout.v_contact, this)

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val padding = resources.getDimensionPixelSize(R.dimen.contact_view_padding)
        setPadding(padding, padding, padding, padding)

        btnRemove.setOnClickListener { onRemoveListener?.onClick(it) }
    }

    fun setContact(contact: Contact?): ContactView {
        setContact(contact, "")
        return this
    }

    fun setContact(contact: Contact?, query: String): ContactView {
        if (contact == null) {
            visibility = View.INVISIBLE
        } else {
            visibility = View.VISIBLE

            if (contact.name.isEmpty()) {
                textContactPhone.visibility = View.GONE
            } else {
                textContactPhone.text = contact.phone
                textContactPhone.visibility = View.VISIBLE
            }

            val title = if (contact.name.isEmpty()) contact.phone else contact.name
            val index = if (query.isEmpty()) -1 else title.indexOf(query, ignoreCase = true)
            if (index >= 0) {
                val spannedContactName = SpannableStringBuilder(title)
                spannedContactName.setSpan(ForegroundColorSpan(context.color(R.color.amber_600)),
                        index, index + query.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                textContactName.text = spannedContactName
            } else {
                textContactName.text = title
            }

            if (contact.getPhotoUri() == null) {
                imageContactPhoto.setImageResource(R.drawable.ic_default_avatar_fg)
                imageContactPhoto.scaleType = ImageView.ScaleType.CENTER
            } else {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, contact.getPhotoUri())
                    val drawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    drawable.cornerRadius = Math.max(bitmap.width, bitmap.height) / 2.0f
                    imageContactPhoto.setImageDrawable(drawable)
                    imageContactPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                } catch (e: FileNotFoundException) {
                    imageContactPhoto.setImageResource(R.drawable.ic_default_avatar_fg)
                    imageContactPhoto.scaleType = ImageView.ScaleType.CENTER
                }
            }
        }
        return this
    }
}