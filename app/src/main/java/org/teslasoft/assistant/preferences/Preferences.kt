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

class Preferences private constructor(private var preferences: SharedPreferences, private var chatId: String) {
    companion object {
        private var preferences: Preferences? = null
        public fun getPreferences(context: Context, chatId: String) : Preferences {
            if (preferences == null) preferences = Preferences(context.getSharedPreferences("settings.$chatId", Context.MODE_PRIVATE), chatId)
            else {
                if (preferences?.chatId != chatId) {
                    preferences?.setPreferences(chatId, context)
                }
            }

            return preferences!!
        }
    }

    /**
     * Sets the shared preferences file for the given chat ID in the context provided.
     *
     * @param chatId The chat ID for which the settings are to be set.
     * @param context The context in which the shared preferences will be accessed.
     */
    fun setPreferences(chatId: String, context: Context) {
        this.preferences = context.getSharedPreferences("settings.$chatId", Context.MODE_PRIVATE)
    }

    /**
     * Retrieves a String value from the shared preferences.
     *
     * @param param The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The value associated with the specified key or the default value if the key is not found.
     */
    private fun getString(param: String?, default: String?) : String {
        return preferences.getString(param, default).toString()
    }

    /**
     * Puts a String value in the shared preferences.
     *
     * @param param The key with which the value is to be associated.
     * @param value The value to be stored.
     */
    private fun putString(param: String, value: String) {
        preferences.edit().putString(param, value).apply()
    }

    /**
     * Retrieves a Boolean value from the shared preferences.
     *
     * @param param The key of the value to retrieve.
     * @param default The default value to return if the key is not found.
     * @return The value associated with the specified key or the default value if the key is not found.
     */
    private fun getBoolean(param: String?, default: Boolean) : Boolean {
        return preferences.getBoolean(param, default)
    }

    /**
     * Puts a Boolean value in the shared preferences.
     *
     * @param param The key with which the value is to be associated.
     * @param value The value to be stored.
     */
    private fun putBoolean(param: String, value: Boolean) {
        preferences.edit().putBoolean(param, value).apply()
    }

    /**
     * Retrieves the model name from the shared preferences.
     *
     * @return The model name or "gpt-3.5-turbo" if not found.
     */
    fun getModel() : String {
        return getString("model", "gpt-3.5-turbo")
    }

    /**
     * Sets the model name in the shared preferences.
     *
     * @param model The model name to be stored.
     */
    fun setModel(model: String) {
        putString("model", model)
    }

    /**
     * Retrieves the max tokens value from the shared preferences.
     *
     * @return The maximum token value or 1500 if not found.
     */
    fun getMaxTokens() : Int {
        return getString("max_tokens", "1500").toInt()
    }

    /**
     * Sets the max tokens value in the shared preferences.
     *
     * @param tokens The maximum token value to be stored.
     */
    fun setMaxTokens(tokens: Int) {
        putString("max_tokens", tokens.toString())
    }

    /**
     * Retrieves the resolution from the shared preferences.
     *
     * @return The resolution value or "512x512" if not found.
     */
    fun getResolution() : String {
        return getString("resolution", "512x512")
    }

    /**
     * Sets the resolution in the shared preferences.
     *
     * @param resolution The resolution value to be stored.
     */
    fun setResolution(resolution: String) {
        putString("resolution", resolution)
    }

    /**
     * Retrieves the silence mode status from the shared preferences.
     *
     * @return The silence mode status, true if enabled or false otherwise.
     */
    fun getSilence() : Boolean {
        return getBoolean("silence_mode", false)
    }

    /**
     * Sets silence mode.
     *
     * @param mode mode.
     */
    fun setSilence(mode: Boolean) {
        putBoolean("silence_mode", mode)
    }

    /**
     * Retrieves the always speak mode status from the shared preferences.
     *
     * @return The always speak mode status, true if enabled or false otherwise.
     */
    fun getNotSilence() : Boolean {
        return getBoolean("always_speak_mode", false)
    }

    /**
     * Sets always speak mode.
     *
     * @param mode mode.
     */
    fun setNotSilence(mode: Boolean) {
        putBoolean("always_speak_mode", mode)
    }

    /**
     * Retrieves the function calling status from the shared preferences.
     *
     * @return The function calling status, true if enabled or false otherwise.
     */
    fun getFunctionCalling() : Boolean {
        return getBoolean("function_calling", false)
    }

    /**
     * Sets function calling mode.
     *
     * @param mode mode.
     */
    fun setFunctionCalling(mode: Boolean) {
        putBoolean("function_calling", mode)
    }

    /**
     * Retrieves the imagine command status from the shared preferences.
     *
     * @return The imagine command status, true if enabled or false otherwise.
     */
    fun getImagineCommand() : Boolean {
        return getBoolean("imagine_command", false)
    }

    /**
     * Enable/disable imagine command.
     *
     * @param mode mode.
     */
    fun setImagineCommand(mode: Boolean) {
        putBoolean("imagine_command", mode)
    }

    /**
     * Retrieves the auto language detection status in the shared preferences.
     *
     * @return uto language detection status to be stored (true for enabled, false otherwise).
     */
    fun getAutoLangDetect() : Boolean {
        return getBoolean("autoLangDetect", false)
    }

    /**
     * Sets the auto language detection status in the shared preferences.
     *
     * @param mode - The auto language detection status to be stored (true for enabled, false otherwise).
     */
    fun setAutoLangDetect(mode: Boolean) {
        putBoolean("autoLangDetect", mode)
    }

    /**
     * Retrieves the end separator from the shared preferences.
     *
     * @return The end separator value or an empty String if not found.
     */
    fun getEndSeparator() : String {
        return getString("end", "")
    }

    /**
     * Sets the end separator in the shared preferences.
     *
     * @param separator The end separator value to be stored.
     */
    fun setEndSeparator(separator: String) {
        putString("end", separator)
    }

    /**
     * Retrieves the prefix from the shared preferences.
     *
     * @return The prefix value or an empty String if not found.
     */
    fun getPrefix() : String {
        return getString("prefix", "")
    }

    /**
     * Sets the prefix in the shared preferences.
     *
     * @param prefix The prefix value to be stored.
     */
    fun setPrefix(prefix: String) {
        putString("prefix", prefix)
    }

    /**
     * Retrieves the audio model from the shared preferences.
     *
     * @return The audio model value or "google" if not found.
     */
    fun getAudioModel() : String {
        return getString("audio", "google")
    }

    /**
     * Sets the audio model in the shared preferences.
     *
     * @param model The audio model value to be stored.
     */
    fun setAudioModel(model: String) {
        putString("audio", model)
    }

    /**
     * Retrieves the prompt from the shared preferences.
     *
     * @return The prompt value or an empty String if not found.
     */
    fun getPrompt() : String {
        return getString("prompt", "")
    }

    /**
     * Sets the prompt in the shared preferences.
     *
     * @param prompt The prompt value to be stored.
     */
    fun setPrompt(prompt: String) {
        putString("prompt", prompt)
    }

    /**
     * Retrieves the language from the shared preferences.
     *
     * @return The language value or an english if not found.
     */
    fun getLanguage() : String {
        return getString("lang", "en")
    }

    /**
     * Sets the language in the shared preferences.
     *
     * @param lang The language value to be stored.
     */
    fun setLanguage(lang: String) {
        putString("lang", lang)
    }

    /**
     * Retrieves the layout mode from the shared preferences.
     *
     * @return The layout mode or "classic" if not found.
     */
    fun getLayout() : String {
        return getString("layout", "classic")
    }

    /**
     * Sets the layout mode in the shared preferences.
     *
     * @param layout The layout mode to be stored.
     */
    fun setLayout(layout: String) {
        putString("layout", layout)
    }

    /**
     * Retrieves the voice model.
     *
     * @return voice model.
     */
    fun getVoice() : String {
        return getString("voice", "en-us-x-iom-network")
    }

    /**
     * Sets the voice model.
     *
     * @param model voice model.
     */
    fun setVoice(model: String) {
        putString("voice", model)
    }

    /**
     * Retrieves the encrypted API key from the shared preferences.
     *
     * @param context The context to access the encrypted shared preferences.
     * @return The decrypted API key or an empty String if not found.
     */
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

    /**
     * Sets the encrypted API key in the shared preferences.
     *
     * @param key The API key to be stored in an encrypted form.
     * @param context The context to access the encrypted shared preferences.
     */
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

    /**
     * Retrieves the old (non-encrypted) API key from the shared preferences.
     *
     * @return The old API key or an empty String if not found.
     */
    @Deprecated("Should be removed in future releases")
    fun getOldApiKey() : String {
        return getString("api_key", "")
    }

    /**
     * Sets the API key to the value of the old API key, if it exists.
     * This method is used to migrate to a new API key storage system.
     * It retrieves the old API key from the preferences, sets it to the new API key storage system,
     * and removes it from the old storage system.
     *
     * @param context The context used to access the preferences.
     */
    fun secureApiKey(context: Context) {
        if (getOldApiKey() != "") {
            setApiKey(getOldApiKey(), context)
            preferences.edit().remove("api_key").apply()
        }
    }
}
