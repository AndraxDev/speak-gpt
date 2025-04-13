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
import org.teslasoft.assistant.util.Hash

class Preferences private constructor(private var preferences: SharedPreferences, private var gp: SharedPreferences, private var chatId: String) {
    companion object {
        private var preferences: Preferences? = null
        fun getPreferences(context: Context, xchatId: String) : Preferences {
            if (preferences == null) preferences = Preferences(context.getSharedPreferences("settings.$xchatId", Context.MODE_PRIVATE), context.getSharedPreferences("settings", Context.MODE_PRIVATE), xchatId)

            else {
                if (preferences?.chatId != xchatId) {
                    preferences?.setPreferences(xchatId, context)
                }
            }

            return preferences!!
        }
    }

    fun getChatId() : String {
        return chatId
    }

    fun interface PreferencesChangedListener {
        fun onPreferencesChanged(key: String, value: String)
    }

    private var listeners: ArrayList<PreferencesChangedListener> = ArrayList()

    fun addOnPreferencesChangedListener(listener: PreferencesChangedListener): Preferences {
        listeners.add(listener)
        return this
    }

    fun forceUpdate() {
        for (listener in listeners) {
            listener.onPreferencesChanged("forceUpdate", "true")
        }
    }

    /**
     * Sets the shared preferences file for the given chat ID in the context provided.
     *
     * @param chatId The chat ID for which the settings are to be set.
     * @param context The context in which the shared preferences will be accessed.
     */
    fun setPreferences(chatId: String, context: Context) {
        this.chatId = chatId
        this.preferences = context.getSharedPreferences("settings.$chatId", Context.MODE_PRIVATE)
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
     * Removes a key from the global shared preferences.
     *
     * @param param The key to be removed.
     */
    private fun removeKey(param: String) {
        gp.edit()?.remove(param)?.apply()
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
    private fun putString(param: String, value: String, default: String = "") {
        val oldValue = getString(param, default)

        if (oldValue != value) {
            preferences.edit().putString(param, value).apply()

            for (listener in listeners) {
                listener.onPreferencesChanged(param, value)
            }
        }
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
    private fun putBoolean(param: String, value: Boolean, default: Boolean = false) {
        val oldValue = getBoolean(param, default)

        if (oldValue != value) {
            preferences.edit().putBoolean(param, value).apply()

            for (listener in listeners) {
                listener.onPreferencesChanged(param, value.toString())
            }
        }
    }

    /**
    * Show chat errors
    *
    * @return show chat errors
    * */
    fun showChatErrors() : Boolean {
        return getGlobalBoolean("show_chat_errors", true)
    }

    /**
     * Set show chat errors
     *
     * @param state show chat errors
     * */
    fun setShowChatErrors(state: Boolean) {
        putGlobalBoolean("show_chat_errors", state, true)
    }

    /**
     * Retrieves the model name from the shared preferences.
     *
     * @return The model name or "gpt-4o" if not found. GPT4-o is now much more capable than gpt 3.5 and 15x cheaper.
     */
    fun getModel() : String {
        var model = getString("model", "gpt-4o")

        // Migrate from legacy dated model
        if (model == "gpt-4-1106-preview") model = "gpt-4-turbo-preview"
        return model
    }

    /**
     * Sets the model name in the shared preferences.
     *
     * @param model The model name to be stored.
     */
    fun setModel(model: String) {
        // Migrate from legacy dated model
        if (model == "gpt-4-1106-preview") {
            putString("model", "gpt-4-turbo-preview")
        } else {
            putString("model", model)
        }
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
     * @return The resolution value or "1024x1024" if not found.
     */
    fun getResolution() : String {
        return getString("resolution", "1024x1024")
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
     * Retrieves the imagine command status from the shared preferences.
     *
     * @return The imagine command status, true if enabled or false otherwise.
     */
    fun getImagineCommand() : Boolean {
        return getBoolean("imagine_command", true)
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
     * Retrieves the hide model names status from the shared preferences.
     *
     * @return The hide model names status, true if enabled or false otherwise.
     */
    fun getHideModelNames() : Boolean {
        try {
            return getGlobalString("hide_model_names", "true") == "true"
        } catch (e: Exception) {
            val hideModelNames = getGlobalBoolean("hide_model_names", false)
            removeKey("hide_model_names")
            putGlobalString("hide_model_names", if (hideModelNames) "true" else "false")
            return hideModelNames
        }
    }

    /**
     * Enable/disable hide model names.
     *
     * @param state mode.
     */
    fun setHideModelNames(state: Boolean) {
        try {
            putGlobalString("hide_model_names", if (state) "true" else "false")
        } catch (e: Exception) {
            removeKey("hide_model_names")
            putGlobalString("hide_model_names", if (state) "true" else "false")
        }
    }

    /**
     * Retrieves the monochrome background for chat list status from the shared preferences.
     *
     * @return The monochrome background for chat list status, true if enabled or false otherwise.
     */
    fun getMonochromeBackgroundForChatList() : Boolean {
        return getGlobalBoolean("monochrome_background_for_chat_list", false)
    }

    /**
     * Enable/disable monochrome background for chat list.
     *
     * @param state mode.
     */
    fun setMonochromeBackgroundForChatList(state: Boolean) {
        putGlobalBoolean("monochrome_background_for_chat_list", state)
    }

    /**
     * Retrieves the custom host URL for API requests.
     *
     * @return The custom host URL as a string. If no custom host URL is set, it returns the default value "https://api.openai.com".
     */
    @Deprecated("Use ApiEndpointPreferences instead")
    fun getCustomHost() : String {
        return getGlobalString("custom_host", "https://api.openai.com/v1/")
    }

    /**
     * Sets the custom host URL for API requests.
     *
     * @param host The custom host URL to be set.
     */
    @Deprecated("Use ApiEndpointPreferences instead")
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
     * Retrieves system message. System messages allow you to make ChatGPT more reliable.
     *
     * @return System message.
     */
    fun getSystemMessage() : String {
        return getString("system_message", "")
    }

    /**
     * Sets system message. System messages allow you to make ChatGPT more reliable.
     *
     * @param message The system message.
     */
    fun setSystemMessage(message: String) {
        putString("system_message", message)
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
     * Sets the assistant name in the shared preferences.
     *
     * @param name The assistant name value to be stored.
     */
    fun setAssistantName(name: String) {
        putString("assistant_name", name)
    }

    /**
     * Retrieves the assistant name from the shared preferences.
     *
     * @return The assistant name value or "Assistant" if not found.
     */
    fun getAssistantName() : String {
        return getString("assistant_name", "SpeakGPT")
    }

    /**
     * Sets the avatar type in the shared preferences.
     *
     * @param type The avatar value (file/builtin/url) to be stored.
     */
    fun setAvatarType(type: String) {
        putString("avatar_type", type)
        listeners.forEach { it.onPreferencesChanged("avatar_type", type) }
    }

    /**
     * Retrieves the avatar type from the shared preferences.
     *
     * @return The avatar type value or "Assistant" if not found.
     */
    fun getAvatarType() : String {
        return getString("avatar_type", "builtin")
    }

    fun getAvatarTypeByChatId(chatId: String, context: Context) : String {
        val sharedPreferences = context.getSharedPreferences("settings.$chatId", Context.MODE_PRIVATE)
        return sharedPreferences.getString("avatar_type", "builtin").toString()
    }

    /**
     * Sets the avatar Id in the shared preferences.
     *
     * @param id The avatar Id value to be stored.
     */
    fun setAvatarId(id: String) {
        listeners.forEach { it.onPreferencesChanged("avatar_id", id) }
        putString("avatar_id", id)
    }

    /**
     * Retrieves the avatar Id from the shared preferences.
     *
     * @return The avatar Id value or "speakgpt" if not found.
     */
    fun getAvatarId() : String {
        return getString("avatar_id", "gpt")
    }

    fun getAvatarIdByChatId(chatId: String, context: Context) : String {
        val sharedPreferences = context.getSharedPreferences("settings.$chatId", Context.MODE_PRIVATE)
        return sharedPreferences.getString("avatar_id", "gpt").toString()
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
     * Set TTS engine
     *
     * @param engine - TTS engine (google or openai)
     * */
    fun setTtsEngine(engine: String) {
        putString("tts_engine", engine)
    }

    /**
     * Get TTS engine
     *
     * @return TTS engine (google or openai)
     * */
    fun getTtsEngine() : String {
        return getString("tts_engine", "google")
    }

    /**
     * Set OpenAI voice
     *
     * @param voice - voice name
     * */
    fun setOpenAIVoice(voice: String) {
        putString("openai_voice", voice)
    }

    /**
     * Get OpenAI voice
     *
     * @return voice name
     * */
    fun getOpenAIVoice() : String {
        return getString("openai_voice", "alloy")
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
     * Get dalle version (2 or 3, 3 is default)
     *
     * @return dalle version
     * */
    fun getDalleVersion() : String {
        return getString("dalle_version", "3")
    }

    /**
     * Set dalle version (2 or 3, 2 is default)
     *
     * @param version dalle version
     * */
    fun setDalleVersion(version: String) {
        putString("dalle_version", version)
    }

    /**
     * Set temperature. Min value 0, max 2
     *
     * @param temperature temperature
     * */
    fun setTemperature(temperature: Float) {
        putString("temperature", temperature.toString())
    }

    /**
     * Set frequency penalty. Min value -2, max 2
     *
     * @param frequencyPenalty frequency penalty
     * */
    fun setFrequencyPenalty(frequencyPenalty: Float) {
        putString("frequency_penalty", frequencyPenalty.toString())
    }

    /**
     * Get frequency penalty. Min value -2, max 2
     *
     * @return frequency penalty
     * */
    fun getFrequencyPenalty() : Float {
        return getString("frequency_penalty", "0.0").toFloat()
    }

    /**
     * Set presence penalty. Min value -2, max 2
     *
     * @param presencePenalty presence penalty
     * */
    fun setPresencePenalty(presencePenalty: Float) {
        putString("presence_penalty", presencePenalty.toString())
    }

    /**
     * Get presence penalty. Min value -2, max 2
     *
     * @return presence penalty
     * */
    fun getPresencePenalty() : Float {
        return getString("presence_penalty", "0.0").toFloat()
    }

    /**
     * Get temperature. Min value 0, max 2
     *
     * @return temperature
     * */
    fun getTemperature() : Float {
        return getString("temperature", "0.7").toFloat()
    }

    /**
     * Set top P. Min value 0 max 1
     *
     * @param topP top P
     * */
    fun setTopP(topP: Float) {
        putString("topP", topP.toString())
    }

    /**
     * Get top P. Min value 0 max 1
     *
     * @return top P
     * */
    fun getTopP() : Float {
        return getString("topP", "1").toFloat()
    }

    /**
     * Set seed
     *
     * @param seed seed
     * */
    fun setSeed(seed: String) {
        putString("seed", seed)
    }

    /**
     * Get seed
     *
     * @return seed
     * */
    fun getSeed() : String {
        return getString("seed", "")
    }

    /**
     * Automatically send messages after voice input is complete
     *
     * @return auto send
     * */
    fun autoSend() : Boolean {
        return getGlobalBoolean("auto_send", true)
    }

    /**
     * Automatically send messages after voice input is complete
     *
     * @param state auto send
     * */
    fun setAutoSend(state: Boolean) {
        putGlobalBoolean("auto_send", state, true)
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
    @Deprecated("Use ApiEndpointPreferences instead")
    fun getApiKey(context: Context) : String {
        return EncryptedPreferences.getEncryptedPreference(context, "api", "api_key")
    }

    /**
     * Sets the encrypted API key in the shared preferences.
     *
     * @param key The API key to be stored in an encrypted form.
     * @param context The context to access the encrypted shared preferences.
     */
    @Deprecated("Use ApiEndpointPreferences instead")
    fun setApiKey(key: String, context: Context) {
        EncryptedPreferences.setEncryptedPreference(context, "api", "api_key", key)
    }

    /**
     * Now users can set API endpoints per chat
     *
     * @return API endpoint ID
     * */
    fun getApiEndpointId() : String {
        return getString("api_endpoint_id", Hash.hash("Default"))
    }

    /**
     * Now users can set API endpoints per chat
     *
     * @param id API endpoint ID
     * */
    fun setApiEndpointId(id: String) {
        putString("api_endpoint_id", id)
    }

    /**
     * Get logit biases config ID
     *
     * @return logit biases config ID
     * */
    fun getLogitBiasesConfigId() : String {
        return getString("logit_biases_config_id", "")
    }

    /**
     * Set logit biases config ID
     *
     * @param id logit biases config ID
     * */
    fun setLogitBiasesConfigId(id: String) {
        putString("logit_biases_config_id", id)
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
