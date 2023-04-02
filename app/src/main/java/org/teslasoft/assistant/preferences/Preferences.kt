package org.teslasoft.assistant.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import java.io.File
import java.nio.charset.StandardCharsets

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