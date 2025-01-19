/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.preferences

import android.content.Context
import android.content.SharedPreferences

class GlobalPreferences private constructor(private var gp: SharedPreferences) {
    companion object {
        private var preferences: GlobalPreferences? = null
        fun getPreferences(context: Context) : GlobalPreferences {
            if (preferences == null) preferences = GlobalPreferences(context.getSharedPreferences("settings", Context.MODE_PRIVATE))

            return preferences!!
        }
    }

    fun interface PreferencesChangedListener {
        fun onPreferencesChanged(key: String, value: String)
    }

    private var listeners: ArrayList<PreferencesChangedListener> = ArrayList()

    fun addOnPreferencesChangedListener(listener: PreferencesChangedListener): GlobalPreferences {
        listeners.add(listener)
        return this
    }

    fun forceUpdate() {
        for (listener in listeners) {
            listener.onPreferencesChanged("forceUpdate", "true")
        }
    }

    /**
     * Retrieves a global String value from the shared preferences.
     *
     * @param param The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The value associated with the specified key or the default value if the key is not found.
     */
    private fun getGlobalString(param: String?, default: String?) : String {
        return gp.getString(param, default).toString()
    }

    /**
     * Puts a global String value in the shared preferences.
     *
     * @param param The key with which the value is to be associated.
     * @param value The value to be stored.
     */
    private fun putGlobalString(param: String, value: String, default: String = "") {
        val oldValue = getGlobalString(param, default)

        if (oldValue != value) {
            gp.edit()?.putString(param, value)?.apply()

            for (listener in listeners) {
                listener.onPreferencesChanged(param, value)
            }
        }
    }

    /**
     * Get global boolean
     *
     * @param param The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * */
    private fun getGlobalBoolean(param: String?, default: Boolean) : Boolean {
        return gp.getBoolean(param, default)
    }

    /**
     * Set global boolean
     *
     * @param param The key with which the value is to be associated.
     * @param value The value to be stored.
     * */
    private fun putGlobalBoolean(param: String, value: Boolean, default: Boolean = false) {
        val oldValue = getGlobalBoolean(param, default)

        if (oldValue != value) {
            gp.edit()?.putBoolean(param, value)?.apply()

            for (listener in listeners) {
                listener.onPreferencesChanged(param, value.toString())
            }
        }
    }

    /**
     * Set amoled pitch black mode
     *
     * @param mode amoled pitch black mode
     * */
    fun setAmoledPitchBlack(mode: Boolean) {
        putGlobalBoolean("amoled_pitch_black", mode)
    }

    /**
     * Get amoled pitch black mode
     *
     * @return amoled pitch black mode
     * */
    fun getAmoledPitchBlack() : Boolean {
        return getGlobalBoolean("amoled_pitch_black", false)
    }
}
