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

package ru.rescuesmstracker.data.migration

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migration : RealmMigration {
    private val logTag = "Migration"
    private val migrationRules = arrayOf(MigrationRule2())

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        ((oldVersion + 1)..newVersion)
                .map { version -> Pair(version, migrationRules.find { it.toVersion() == version }) }
                .forEach {
                    val rule = it.second
                    if (rule == null) {
                        throw IllegalStateException("There is no migration rule to version=${it.first}")
                    } else {
                        Log.i(logTag, "Applying rule for version=${it.first}")
                        rule.migrate(realm)
                    }
                }
    }
}