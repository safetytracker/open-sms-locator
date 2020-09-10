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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import ru.rescuesmstracker.extensions.color
import ru.rescuesmstracker.onboarding.FormatUtils
import ru.rst.rescuesmstracker.R

class RSTTimerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    enum class State(@ColorRes val progressTextColorRes: Int,
                     @ColorRes val progressBackgroundColorRes: Int) {
        WAITING(R.color.white, R.color.transparent) {
            override fun getFormattedString(context: Context, timeAgo: String): String
                    = if (timeAgo.isBlank()) "" else context.getString(R.string.timer_sms_sent_min_ago, timeAgo)
        },
        SENDING_OFF(R.color.white, R.color.transparent) {
            override fun getFormattedString(context: Context, timeAgo: String): String
                    = context.getString(R.string.timer_sms_sending_off)
        },
        SENDING_SMS(R.color.white, R.color.transparent) {
            override fun getFormattedString(context: Context, timeAgo: String): String
                    = WAITING.getFormattedString(context, timeAgo)
        },
        FAILED_TO_SEND(R.color.white, R.color.red_500) {
            override fun getFormattedString(context: Context, timeAgo: String): String
                    = context.getString(R.string.timer_sms_sending_failed, timeAgo)
        },
        NO_CONTACT(R.color.red_500, R.color.transparent) {
            override fun getFormattedString(context: Context, timeAgo: String): String =
                    context.getString(R.string.timer_no_contact)
        };

        abstract fun getFormattedString(context: Context, timeAgo: String): String
    }

    val max = 100
    var getUpdate: (() -> Unit)? = null

    private var runnable = object : Runnable {
        override fun run() {
            if (isUpdatePosted) {
                getUpdate?.invoke()
                handler.postDelayed(this, timerUpdateDelayMillis)
            }
        }
    }

    private val timerUpdateDelayMillis = 30L
    private val circularProgress: CircularProgressBar = CircularProgressBar(context, attrs)
    private val progressText: TextView = TextView(context)
    private val formatUtils = FormatUtils(context)

    private val circleBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circleBackgroundBounds = RectF()

    private var isUpdatePosted = false
    private var currentState: State? = null

    var progress: Float = 0f
        set(value) {
            circularProgress.progress = value
            field = value
        }

    init {
        circularProgress.backgroundColor = context.color(R.color.white_30_alpha)
        circularProgress.color = context.color(R.color.white)
        circularProgress.backgroundProgressBarWidth = circularProgress.progressBarWidth
        addView(circularProgress, LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        progressText.gravity = Gravity.CENTER
        progressText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                resources.getDimensionPixelSize(R.dimen.rst_timer_text_size).toFloat())
        val textSidesPadding = resources.getDimensionPixelSize(R.dimen.rst_timer_text_sides_padding)
        progressText.setPadding(textSidesPadding, 0, textSidesPadding, 0)
        progressText.setBackgroundColor(Color.TRANSPARENT)
        addView(progressText, LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        setState(State.WAITING, 0)
    }

    fun setState(newState: State, lastSmsSentTimestamp: Long) {
        circleBackgroundPaint.color = context.color(newState.progressBackgroundColorRes)

        progressText.setTextColor(context.color(newState.progressTextColorRes))
        progressText.text = newState.getFormattedString(context,
                formatUtils.formatTimerMinutes(lastSmsSentTimestamp))
        currentState = newState
    }

    fun start() {
        circularProgress.progress = 0f
        isUpdatePosted = true
        postDelayed(runnable, timerUpdateDelayMillis)
    }

    fun stop() {
        circularProgress.progress = 0f
        isUpdatePosted = false
        removeCallbacks(runnable)
    }

    override fun onDraw(canvas: Canvas) {
        if (circleBackgroundPaint.color != Color.TRANSPARENT) {
            canvas.drawOval(circleBackgroundBounds, circleBackgroundPaint)
        }
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        circleBackgroundBounds.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}