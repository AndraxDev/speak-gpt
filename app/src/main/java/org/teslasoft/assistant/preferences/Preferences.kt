/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Preferences private constructor(private var preferences: SharedPreferences) {
    companion object {
        private var preferences: Preferences? = null
        public fun getPreferences(context: Context) : Preferences {
            if (preferences == null) preferences = Preferences(context.getSharedPreferences("settings", Context.MODE_PRIVATE))
            return preferences!!
        }
    }

    private fun getString(param: String?, default: String?) : String {
        return preferences.getString(param, default).toString()
    }

    private fun putString(param: String, value: String) {
        preferences.edit().putString(param, value).apply()
    }

    private fun getBoolean(param: String?, default: Boolean) : Boolean {
        return preferences.getBoolean(param, default)
    }

    private fun putBoolean(param: String, value: Boolean) {
        preferences.edit().putBoolean(param, value).apply()
    }

    fun getModel() : String {
        return getString("model", "gpt-3.5-turbo")
    }

    fun setModel(model: String) {
        putString("model", model)
    }

    fun getMaxTokens() : Int {
        return getString("max_tokens", "1500").toInt()
    }

    fun setMaxTokens(tokens: Int) {
        putString("max_tokens", tokens.toString())
    }

    fun getResolution() : String {
        return getString("resolution", "512x512")
    }

    fun setResolution(resolution: String) {
        putString("resolution", resolution)
    }

    fun getSilence() : Boolean {
        return getBoolean("silence_mode", false)
    }

    public fun setSilence(mode: Boolean) {
        putBoolean("silence_mode", mode)
    }

    fun getEndSeparator() : String {
        return getString("end", "")
    }

    fun setEndSeparator(separator: String) {
        putString("end", separator)
    }

    fun getPrompt() : String {
        return getString("prompt", "")
    }

    fun setPrompt(prompt: String) {
        putString("prompt", prompt)
    }

    fun getLayout() : String {
        return getString("layout", "classic")
    }

    fun setLayout(layout: String) {
        putString("layout", layout)
    }

    fun getApiKey(context: Context) : String {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "api",
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("api_key", "")!!
    }

    fun setApiKey(key: String, context: Context) {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "api",
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with (sharedPreferences.edit()) {
            putString("api_key", key)
            apply()
        }
    }

    // If an insecure API key is found it will be automatically encrypted and transferred to a new location
    // once app will initialized to provide great user experience without interruptions.
    // Welcome screen will be skipped automatically.
    fun getOldApiKey() : String {
        return getString("api_key", "")
    }

    fun secureApiKey(context: Context) {
        if (getOldApiKey() != "") {
            setApiKey(getOldApiKey(), context)
            preferences.edit().remove("api_key").apply()
        }
    }
}