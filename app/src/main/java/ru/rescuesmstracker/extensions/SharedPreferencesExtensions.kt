package ru.rescuesmstracker.extensions

import android.content.SharedPreferences

fun SharedPreferences.getStringOrDefault(key: String, default: String): String =
        getString(key, default)!!

fun SharedPreferences.getStringOrEmpty(key: String): String = getString(key, "")!!