/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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

    /**
     * Set lock assistant window
     * */
    fun setLockAssistantWindow(value: Boolean) {
        putGlobalBoolean("lock_assistant_window", value)
    }

    /**
     * Get lock assistant window
     * */
    fun getLockAssistantWindow() : Boolean {
        return getGlobalBoolean("lock_assistant_window", false)
    }

    /**
     * Desktop mode - automatically focus message input once chat is opened, press enter to send message, shift+enter to add new line
     *
     * This param is global and applies across all chats and activities in the app
     *
     * @return desktop mode status
     * */
    fun getDesktopMode() : Boolean {
        return getGlobalBoolean("desktopMode", false)
    }

    /**
     * Set desktop mode
     *
     * This param is global and applies across all chats and activities in the app
     *
     * @param mode desktop mode status
     * */
    fun setDesktopMode(mode: Boolean) {
        putGlobalBoolean("desktopMode", mode)
    }

    /**
     * Get experimental UI
     *
     * @return experimental UI mode
     */
    fun getExperimentalUI() : Boolean {
        return getGlobalBoolean("experimentalUI", true)
    }

    /**
     * Set experimental UI
     *
     * @param mode experimental UI mode
     */
    fun setExperimentalUI(mode: Boolean) {
        putGlobalBoolean("experimentalUI", mode)
    }

    /**
     * Retrieves the custom host URL for API requests.
     *
     * @return The custom host URL as a string. If no custom host URL is set, it returns the default value "https://api.openai.com".
     */
    fun getCustomHost() : String {
        return getGlobalString("custom_host", "https://api.openai.com/v1/")
    }

    /**
     * Sets the custom host URL for API requests.
     *
     * @param host The custom host URL to be set.
     */
    fun setCustomHost(host: String) {
        putGlobalString("custom_host", host)
    }

    /**
     * Get debug mode
     * */
    fun getDebugMode() : Boolean {
        return getGlobalBoolean("debug_mode", false)
    }

    /**
     * Set debug mode
     * */
    fun setDebugMode(state: Boolean) {
        putGlobalBoolean("debug_mode", state)
    }

    /**
     * Get ads enabled
     * */
    fun getAdsEnabled() : Boolean {
        return getGlobalBoolean("ads_enabled", true)
    }

    /**
     * Set ads enabled
     * */
    fun setAdsEnabled(state: Boolean) {
        putGlobalBoolean("ads_enabled", state, true)
    }

    /**
     * Retrieves the language from the shared preferences.
     *
     * @return The language value or an english if not found.
     */
    fun getLanguage() : String {
        return getGlobalString("lang", "en")
    }

    /**
     * Sets the language in the shared preferences.
     *
     * @param lang The language value to be stored.
     */
    fun setLanguage(lang: String) {
        putGlobalString("lang", lang)
    }

    /**
     * Get Chats autosave
     * */
    fun getChatsAutosave() : Boolean {
        return getGlobalBoolean("chats_autosave", false)
    }

    /**
     * Set Chats autosave
     * */
    fun setChatsAutosave(state: Boolean) {
        putGlobalBoolean("chats_autosave", state)
    }

    /**
     * Get Skip chat name dialog
     * */
    fun getSkipChatNameDialog() : Boolean {
        return getGlobalBoolean("skip_chat_name_dialog", false)
    }

    /**
     * Set skip chat name dialog
     * */
    fun setSkipChatNameDialog(state: Boolean) {
        putGlobalBoolean("skip_chat_name_dialog", state)
    }

    /**
     * Set debug test ads
     * */
    fun setDebugTestAds(state: Boolean) {
        putGlobalBoolean("debug_test_ads", state)
    }

    /**
     * Get debug test ads
     * */
    fun getDebugTestAds() : Boolean {
        return getGlobalBoolean("debug_test_ads", false)
    }

    /**
     * Get Premium license key
     * */
    fun getPremiumKey(context: Context) : String {
        return EncryptedPreferences.getEncryptedPreference(context, "premium", "license_key")
    }

    /**
     * Set Premium license key
     * */
    fun setPremiumKey(key: String, context: Context) {
        EncryptedPreferences.setEncryptedPreference(context, "premium", "license_key", key)
    }

    /**
     * Retrieves the encrypted API key from the shared preferences.
     *
     * @param context The context to access the encrypted shared preferences.
     * @return The decrypted API key or an empty String if not found.
     */
    fun getApiKey(context: Context) : String {
        return EncryptedPreferences.getEncryptedPreference(context, "api", "api_key")
    }

    /**
     * Sets the encrypted API key in the shared preferences.
     *
     * @param key The API key to be stored in an encrypted form.
     * @param context The context to access the encrypted shared preferences.
     */
    fun setApiKey(key: String, context: Context) {
        EncryptedPreferences.setEncryptedPreference(context, "api", "api_key", key)
    }
}
