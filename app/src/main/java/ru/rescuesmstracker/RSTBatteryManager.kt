package ru.rescuesmstracker

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.support.annotation.RequiresApi
import kotlin.math.roundToInt

object RSTBatteryManager {

    fun getCurrentBatteryLevel(context: Context): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getCurrentBatteryLevel21(context)
            } else {
                getCurrentBatteryLevelBelow21(context)
            }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCurrentBatteryLevel21(context: Context): Int {
        val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getCurrentBatteryLevelBelow21(context: Context): Int {
        val batteryStatus = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return (level * 100.0f / scale).roundToInt()
    }

}