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

package ru.rescuesmstracker

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.rescuesmstracker.analytics.Analytics
import ru.rescuesmstracker.data.migration.Migration
import ru.rescuesmstracker.timer.model.ScheduledSmsModel
import ru.rescuesmstracker.utils.AppUpdateManager

class RSTApplication : Application() {

    private val dbVersion = 2L
    private val dbName = "default.realm"

    override fun onCreate() {
        super.onCreate()
        AppUpdateManager.check(this, null)
        Analytics.init(this)

        Realm.init(this)
        val defaultConfig = RealmConfiguration.Builder()
                .name(dbName)
                .schemaVersion(dbVersion)
                .migration(Migration())
                .build()
        Realm.setDefaultConfiguration(defaultConfig)

        if (ScheduledSmsModel.isSmsSendingEnabled(this)) {
            RSTForegroundService.start(this)
        }
    }
}