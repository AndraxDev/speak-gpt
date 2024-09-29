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

package org.teslasoft.assistant.ui.activities

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.transition.TransitionInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Logger
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.fragments.TileFragment
import org.teslasoft.assistant.ui.fragments.dialogs.ActivationPromptDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.AdvancedSettingsDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.CustomizeAssistantDialog
import org.teslasoft.assistant.ui.fragments.dialogs.LanguageSelectorDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.SelectResolutionFragment
import org.teslasoft.assistant.ui.fragments.dialogs.SystemMessageDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.VoiceSelectorDialogFragment
import org.teslasoft.assistant.util.WindowInsetsUtil
import org.teslasoft.core.auth.AccountSyncListener
import org.teslasoft.core.auth.client.SettingsListener
import org.teslasoft.core.auth.client.SyncListener
import org.teslasoft.core.auth.client.TeslasoftIDClient
import org.teslasoft.core.auth.widget.TeslasoftIDCircledButton
import java.util.EnumSet
import java.util.Locale
import kotlin.math.roundToInt

class SettingsActivity : FragmentActivity() {

    private var tileAccountFragment: TileFragment? = null
    private var tileAssistant: TileFragment? = null
    private var tileApiKey: TileFragment? = null
    private var tileAutoSend: TileFragment? = null
    private var tileVoice: TileFragment? = null
    private var tileVoiceLanguage: TileFragment? = null
    private var tileImageModel: TileFragment? = null
    private var tileImageResolution: TileFragment? = null
    private var tileTTS: TileFragment? = null
    private var tileSTT: TileFragment? = null
    private var tileSilentMode: TileFragment? = null
    private var tileAlwaysSpeak: TileFragment? = null
    private var tileTextModel: TileFragment? = null
    private var tileActivationMessage: TileFragment? = null
    private var tileSystemMessage: TileFragment? = null
    private var tileLangDetect: TileFragment? = null
    private var tileChatLayout: TileFragment? = null
    private var tileFunctionCalling: TileFragment? = null
    private var tileSlashCommands: TileFragment? = null
    private var tileDesktopMode: TileFragment? = null
    private var tileAboutApp: TileFragment? = null
    private var tileClearChat: TileFragment? = null
    private var tileDocumentation: TileFragment? = null
    private var tileAmoledMode: TileFragment? = null
    private var tileLockAssistantWindow: TileFragment? = null
    private var tileCustomize: TileFragment? = null
    private var tileDeleteData: TileFragment? = null
    private var tileSendDiagnosticData: TileFragment? = null
    private var tileRevokeAuthorization: TileFragment? = null
    private var tileGetNewInstallationId: TileFragment? = null
    private var tileCrashLog: TileFragment? = null
    private var tileEventLog: TileFragment? = null
    private var tileChatsAutoSave: TileFragment? = null
    private var tileShowChatErrors: TileFragment? = null
    private var tileHideModelNames: TileFragment? = null
    private var tileMonochromeBackgroundForChatList: TileFragment? = null
//    private var threadLoading: LinearLayout? = null
    private var btnRemoveAds: MaterialButton? = null
    private var root: ScrollView? = null
    private var textGlobal: TextView? = null
    private var btnBack: ImageButton? = null
    private var teslasoftIDCircledButton: TeslasoftIDCircledButton? = null

    private var installationId = ""
    private var androidId = ""
    private var areFragmentsInitialized = false
    private var chatId = ""
    private var preferences: Preferences? = null
    private var model = ""
    private var activationPrompt = ""
    private var systemMessage = ""
    private var language = "en"
    private var resolution = ""
    private var voice = ""
    private var host = ""
    private var ttsEngine = "google"
    private var apiEndpoint: ApiEndpointObject? = null

    private var teslasoftIDClient: TeslasoftIDClient? = null
    private var apiEndpointPreferences: ApiEndpointPreferences? = null

    private var modelChangedListener: AdvancedSettingsDialogFragment.StateChangesListener = object : AdvancedSettingsDialogFragment.StateChangesListener {
        override fun onSelected(name: String, maxTokens: String, endSeparator: String, prefix: String) {
            model = name
            preferences?.setModel(name)
            preferences?.setMaxTokens(maxTokens.toInt())
            preferences?.setEndSeparator(endSeparator)
            preferences?.setPrefix(prefix)
            tileTextModel?.updateSubtitle(model)
        }

        override fun onFormError(name: String, maxTokens: String, endSeparator: String, prefix: String) {
            if (name == "") Toast.makeText(this@SettingsActivity, getString(R.string.model_error_empty), Toast.LENGTH_SHORT).show()
            else if (name.contains("gpt-4")) Toast.makeText(this@SettingsActivity, "Error, GPT4 support maximum of 8192 tokens", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this@SettingsActivity, "Error, more than 2048 tokens is not supported", Toast.LENGTH_SHORT).show()
            val advancedSettingsDialogFragment: AdvancedSettingsDialogFragment = AdvancedSettingsDialogFragment.newInstance(name, chatId)
            advancedSettingsDialogFragment.setStateChangedListener(this)
            advancedSettingsDialogFragment.show(supportFragmentManager.beginTransaction(), "ModelDialog")
        }
    }

    private var languageChangedListener: LanguageSelectorDialogFragment.StateChangesListener = object : LanguageSelectorDialogFragment.StateChangesListener {
        override fun onSelected(name: String) {
            preferences?.setLanguage(name)
            language = name
            tileVoiceLanguage?.updateSubtitle(Locale.forLanguageTag(name).displayLanguage)
        }

        override fun onFormError(name: String) {
            Toast.makeText(this@SettingsActivity, getString(R.string.language_error_empty), Toast.LENGTH_SHORT).show()
            val languageSelectorDialogFragment: LanguageSelectorDialogFragment = LanguageSelectorDialogFragment.newInstance(name, chatId)
            languageSelectorDialogFragment.setStateChangedListener(this)
            languageSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "LanguageSelectorDialog")
        }
    }

    private var resolutionChangedListener: SelectResolutionFragment.StateChangesListener = object : SelectResolutionFragment.StateChangesListener {
        override fun onSelected(name: String) {
            preferences?.setResolution(name)
            resolution = name
            tileImageResolution?.updateSubtitle(name)
        }

        override fun onFormError(name: String) {
            Toast.makeText(this@SettingsActivity, getString(R.string.image_resolution_error_empty), Toast.LENGTH_SHORT).show()
            val resolutionSelectorDialogFragment: SelectResolutionFragment = SelectResolutionFragment.newInstance(name, chatId)
            resolutionSelectorDialogFragment.setStateChangedListener(this)
            resolutionSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "ResolutionSelectorDialog")
        }
    }

    private var promptChangedListener: ActivationPromptDialogFragment.StateChangesListener =
        ActivationPromptDialogFragment.StateChangesListener { prompt ->
            activationPrompt = prompt
            preferences?.setPrompt(prompt)
        }

    private var systemChangedListener: SystemMessageDialogFragment.StateChangesListener =
        SystemMessageDialogFragment.StateChangesListener { prompt ->
            systemMessage = prompt
            preferences?.setSystemMessage(prompt)
        }

    private var voiceSelectorListener: VoiceSelectorDialogFragment.OnVoiceSelectedListener =
        VoiceSelectorDialogFragment.OnVoiceSelectedListener { voice ->
            this@SettingsActivity.voice = voice

            tileVoice?.updateSubtitle(voice)
        }

    private var customizeAssistantDialogListener: CustomizeAssistantDialog.CustomizeAssistantDialogListener = object : CustomizeAssistantDialog.CustomizeAssistantDialogListener {
        override fun onEdit(assistantName: String, avatarType: String, avatarId: String) {
            preferences?.setAssistantName(assistantName)
            preferences?.setAvatarType(avatarType)
            preferences?.setAvatarId(avatarId)
        }

        override fun onError(assistantName: String, avatarType: String, avatarId: String, error: String, dialog: CustomizeAssistantDialog) {
            Toast.makeText(this@SettingsActivity, error, Toast.LENGTH_SHORT).show()
            dialog.show(supportFragmentManager.beginTransaction(), "CustomizeAssistantDialog")
        }

        override fun onCancel() { /* unused */ }
    }

    private var settingsListener: SettingsListener = object : SettingsListener {
        override fun onSuccess(settings: String) {
            super.onSuccess(settings)
        }

        override fun onError(state: String, message: String) {
            super.onError(state, message)
        }
    }

    private var syncListener: SyncListener = object : SyncListener {
        override fun onSuccess() {
            super.onSuccess()
        }

        override fun onError(state: String, message: String) {
            super.onError(state, message)
        }
    }

    private var accountSyncListener: AccountSyncListener = object : AccountSyncListener {
        override fun onAuthFinished(name: String, email: String, isDev: Boolean, token: String) {
            Thread {
                Thread.sleep(500)
                runOnUiThread {
                    if (isDev) {
                        preferences?.setDebugMode(true)
                    } else {
                        preferences?.setDebugMode(false)
                    }
                }
            }.start()
        }

        override fun onAuthFailed(state: String, message: String) {
            runOnUiThread {
                preferences?.setDebugMode(false)
            }
        }

        override fun onSignedOut() {
            runOnUiThread {
                preferences?.setDebugMode(false)
                restartActivity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 30) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 500
        }

        transition.excludeTarget(R.id.scrollable, true)
        transition.excludeTarget(R.id.text_global, true)
        transition.excludeTarget(R.id.textView30, true)
        transition.excludeTarget(R.id.textView31, true)
        transition.excludeTarget(R.id.textView32, true)
        transition.excludeTarget(R.id.textView33, true)
        transition.excludeTarget(R.id.textView34, true)
        transition.excludeTarget(R.id.textView35, true)
        transition.excludeTarget(R.id.textView36, true)
        transition.excludeTarget(R.id.textView387, true)
        transition.excludeTarget(R.id.textView389, true)
        transition.excludeTarget(R.id.constraintLayout8, true)
        transition.excludeTarget(R.id.constraintLayout9, true)
        transition.excludeTarget(R.id.constraintLayout10, true)
        transition.excludeTarget(R.id.constraintLayout11, true)
        transition.excludeTarget(R.id.constraintLayout12, true)
        transition.excludeTarget(R.id.constraintLayout13, true)
        transition.excludeTarget(R.id.constraintLayout14, true)
        transition.excludeTarget(R.id.constraintLayout16, true)
        transition.excludeTarget(R.id.constraintLayout17, true)
        transition.excludeTarget(R.id.constraintLayout167, true)
        transition.excludeTarget(R.id.activity_new_settings_title, true)
        transition.excludeTarget(R.id.btn_back, true)
        transition.excludeTarget(R.id.tile_account, true)
        transition.excludeTarget(R.id.tile_assistant, true)
        transition.excludeTarget(R.id.tile_api, true)
        transition.excludeTarget(R.id.tile_autosend, true)
        transition.excludeTarget(R.id.tile_voice, true)
        transition.excludeTarget(R.id.tile_voice_language, true)
        transition.excludeTarget(R.id.tile_image_model, true)
        transition.excludeTarget(R.id.tile_image_resolution, true)
        transition.excludeTarget(R.id.tile_tts, true)
        transition.excludeTarget(R.id.tile_stt, true)
        transition.excludeTarget(R.id.tile_silent_mode, true)
        transition.excludeTarget(R.id.tile_always_speak, true)
        transition.excludeTarget(R.id.tile_text_model, true)
        transition.excludeTarget(R.id.tile_activation_prompt, true)
        transition.excludeTarget(R.id.tile_system_message, true)
        transition.excludeTarget(R.id.tile_auto_language_detection, true)
        transition.excludeTarget(R.id.tile_chat_layout, true)
        transition.excludeTarget(R.id.tile_function_calling, true)
        transition.excludeTarget(R.id.tile_slash_commands, true)
        transition.excludeTarget(R.id.tile_desktop_mode, true)
        transition.excludeTarget(R.id.tile_about_app, true)
        transition.excludeTarget(R.id.tile_clear_chat, true)
        transition.excludeTarget(R.id.tile_documentation, true)
        transition.excludeTarget(R.id.tile_amoled_mode, true)
        transition.excludeTarget(R.id.tile_lock_assistant, true)
        transition.excludeTarget(R.id.tile_customize, true)
        transition.excludeTarget(R.id.tile_delete_data, true)
        transition.excludeTarget(R.id.tile_send_diagnostic_data, true)
        transition.excludeTarget(R.id.tile_revoke_authorization, true)
        transition.excludeTarget(R.id.tile_assign_new_id, true)
        transition.excludeTarget(R.id.tile_crash_log, true)
        transition.excludeTarget(R.id.tile_event_log, true)
        transition.excludeTarget(R.id.tile_chats_autosave, true)
        transition.excludeTarget(R.id.tile_show_chat_errors, true)
        transition.excludeTarget(R.id.tile_hide_model_names, true)
        transition.excludeTarget(R.id.tile_monochrome_background_for_chat_list, true)
        transition.excludeTarget(R.id.teslasoft_id_btn, true)

        val transition2 = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = FastOutLinearInInterpolator()
            duration = 200
        }

        transition2.excludeTarget(R.id.scrollable, true)
        transition2.excludeTarget(R.id.text_global, true)
        transition2.excludeTarget(R.id.textView30, true)
        transition2.excludeTarget(R.id.textView31, true)
        transition2.excludeTarget(R.id.textView32, true)
        transition2.excludeTarget(R.id.textView33, true)
        transition2.excludeTarget(R.id.textView34, true)
        transition2.excludeTarget(R.id.textView35, true)
        transition2.excludeTarget(R.id.textView36, true)
        transition2.excludeTarget(R.id.textView387, true)
        transition2.excludeTarget(R.id.textView389, true)
        transition2.excludeTarget(R.id.constraintLayout8, true)
        transition2.excludeTarget(R.id.constraintLayout9, true)
        transition2.excludeTarget(R.id.constraintLayout10, true)
        transition2.excludeTarget(R.id.constraintLayout11, true)
        transition2.excludeTarget(R.id.constraintLayout12, true)
        transition2.excludeTarget(R.id.constraintLayout13, true)
        transition2.excludeTarget(R.id.constraintLayout14, true)
        transition2.excludeTarget(R.id.constraintLayout16, true)
        transition2.excludeTarget(R.id.constraintLayout17, true)
        transition2.excludeTarget(R.id.constraintLayout167, true)
        transition2.excludeTarget(R.id.tile_account, true)
        transition2.excludeTarget(R.id.tile_assistant, true)
        transition2.excludeTarget(R.id.tile_api, true)
        transition2.excludeTarget(R.id.tile_autosend, true)
        transition2.excludeTarget(R.id.tile_voice, true)
        transition2.excludeTarget(R.id.tile_voice_language, true)
        transition2.excludeTarget(R.id.tile_image_model, true)
        transition2.excludeTarget(R.id.tile_image_resolution, true)
        transition2.excludeTarget(R.id.tile_tts, true)
        transition2.excludeTarget(R.id.tile_stt, true)
        transition2.excludeTarget(R.id.tile_silent_mode, true)
        transition2.excludeTarget(R.id.tile_always_speak, true)
        transition2.excludeTarget(R.id.tile_text_model, true)
        transition2.excludeTarget(R.id.tile_activation_prompt, true)
        transition2.excludeTarget(R.id.tile_system_message, true)
        transition2.excludeTarget(R.id.tile_auto_language_detection, true)
        transition2.excludeTarget(R.id.tile_chat_layout, true)
        transition2.excludeTarget(R.id.tile_function_calling, true)
        transition2.excludeTarget(R.id.tile_slash_commands, true)
        transition2.excludeTarget(R.id.tile_desktop_mode, true)
        transition2.excludeTarget(R.id.tile_about_app, true)
        transition2.excludeTarget(R.id.tile_clear_chat, true)
        transition2.excludeTarget(R.id.tile_documentation, true)
        transition2.excludeTarget(R.id.tile_amoled_mode, true)
        transition2.excludeTarget(R.id.tile_lock_assistant, true)
        transition2.excludeTarget(R.id.tile_customize, true)
        transition2.excludeTarget(R.id.tile_delete_data, true)
        transition2.excludeTarget(R.id.tile_send_diagnostic_data, true)
        transition2.excludeTarget(R.id.tile_revoke_authorization, true)
        transition2.excludeTarget(R.id.tile_assign_new_id, true)
        transition2.excludeTarget(R.id.tile_crash_log, true)
        transition2.excludeTarget(R.id.tile_event_log, true)
        transition2.excludeTarget(R.id.tile_chats_autosave, true)
        transition2.excludeTarget(R.id.tile_show_chat_errors, true)
        transition2.excludeTarget(R.id.tile_hide_model_names, true)
        transition2.excludeTarget(R.id.tile_monochrome_background_for_chat_list, true)
        transition2.excludeTarget(R.id.teslasoft_id_btn, true)

        // Set the transition as the shared element enter transition
        window.sharedElementEnterTransition = transition
        window.sharedElementExitTransition = transition2

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                finishActivity()
            }
        }

        val expandableWindow = findViewById<LinearLayout>(R.id.expandable_window)
        expandableWindow.backgroundTintList = ColorStateList.valueOf(SurfaceColors.SURFACE_1.getColor(this))

        btnBack = findViewById(R.id.btn_back)
        root = findViewById(R.id.root)
        textGlobal = findViewById(R.id.text_global)
        btnRemoveAds?.visibility = View.GONE

        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            if (chatId == "") {
                tileClearChat?.setEnabled(false)
                tileClearChat?.setVisibility(TileFragment.TileVisibility.GONE)
                textGlobal?.visibility = TextView.VISIBLE
            } else {
                textGlobal?.visibility = TextView.GONE
            }
        } else {
            tileClearChat?.setEnabled(false)
            tileClearChat?.setVisibility(TileFragment.TileVisibility.GONE)
            textGlobal?.visibility = TextView.VISIBLE
        }

        preferences = Preferences.getPreferences(this, chatId)
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
        apiEndpoint = apiEndpointPreferences?.getApiEndpoint(this, preferences?.getApiEndpointId()!!)

        model = preferences?.getModel() ?: "gpt-3.5-turbo"
        activationPrompt = preferences?.getPrompt() ?: ""
        systemMessage = preferences?.getSystemMessage() ?: ""
        language = preferences?.getLanguage() ?: "en"
        resolution = preferences?.getResolution() ?: "256x256"
        voice = if (preferences?.getTtsEngine() == "google") preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: ""

        host = apiEndpoint?.host ?: ""
        ttsEngine = preferences?.getTtsEngine() ?: "google"

        initTeslasoftID()

        reloadAmoled()

        teslasoftIDClient = TeslasoftIDClient(this, "B7:9F:CB:D0:5C:69:1D:C7:DD:5C:36:50:64:1E:9B:32:00:CA:11:41:47:ED:F1:D9:64:86:2A:CA:49:CD:65:25", "d07985975904997990790c2e5088372a", "org.teslasoft.assistant", settingsListener, syncListener)

        val t1 = Thread {
            androidId = DeviceInfoProvider.getAndroidId(this@SettingsActivity)
            createFragments1()
            createFragments2()
            createFragments3()
            createFragments4()
            createFragments5()
        }

        val t2 = Thread {
            installationId = DeviceInfoProvider.getInstallationId(this@SettingsActivity)
            createFragments6()
            createFragments7()
        }

        t1.start()
        t2.start()

        t1.join()

        Thread {
            t2.join()
            val fragmentTransaction = placeFragments()

            runOnUiThread {
                val t = Thread {
                    runOnUiThread {
                        fragmentTransaction.commit()
                    }
                }

                t.start()
                t.join()

                Thread {
                    Thread.sleep(100)
                    areFragmentsInitialized = true
                }.start()

                initializeLogic()
                adjustPaddings()

                if (chatId == "") {
                    tileClearChat?.setEnabled(false)
                    tileClearChat?.setVisibility(TileFragment.TileVisibility.GONE)
                } else {
                    textGlobal?.visibility = TextView.GONE
                    tileClearChat?.setEnabled(true)
                    tileClearChat?.setVisibility(TileFragment.TileVisibility.VISIBLE)
                }
            }
        }.start()
    }

    private fun createFragments1() {
        val t1 = Thread {
            tileAccountFragment = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_account_title),
                null,
                getString(R.string.tile_account_subtitle),
                null,
                R.drawable.ic_user,
                false,
                chatId,
                getString(R.string.tile_account_desc)
            )

            tileAssistant = TileFragment.newInstance(
                isDefaultAssistantApp(this@SettingsActivity),
                false,
                getString(R.string.tile_assistant_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_assistant,
                false,
                chatId,
                getString(R.string.tile_assistant_desc)
            )

            tileApiKey = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_api_endpoint_title),
                null,
                host,
                null,
                R.drawable.ic_key,
                false,
                chatId,
                getString(R.string.tile_api_endpoint_desc)
            )

            tileAutoSend = TileFragment.newInstance(
                preferences?.autoSend()!!,
                true,
                getString(R.string.tile_autosend_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_send,
                false,
                chatId,
                getString(R.string.tile_autosend_desc)
            )

            tileVoice = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_tts_voice_title),
                null,
                voice,
                null,
                R.drawable.ic_voice,
                false,
                chatId,
                getString(R.string.tile_tts_voice_desc)
            )
        }

        t1.start()
        t1.join()
    }

    private fun createFragments2() {
        val t2 = Thread {
            tileVoiceLanguage = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_voice_lang_title),
                null,
                Locale.forLanguageTag(preferences?.getLanguage()!!).displayLanguage,
                null,
                R.drawable.ic_language,
                false,
                chatId,
                getString(R.string.tile_voice_lang_desc)
            )

            tileImageModel = TileFragment.newInstance(
                preferences?.getDalleVersion() == "3",
                true,
                getString(R.string.tile_dalle_3),
                null,
                getString(R.string.on),
                getString(R.string.tile_dalle_2),
                R.drawable.ic_image,
                false,
                chatId,
                getString(R.string.tile_dalle_desc)
            )

            tileImageResolution = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_image_resolution_title),
                null,
                preferences?.getResolution()!!,
                null,
                R.drawable.ic_image,
                false,
                chatId,
                getString(R.string.tile_image_resolution_desc)
            )

            tileTTS = TileFragment.newInstance(
                preferences?.getTtsEngine() == "openai",
                true,
                getString(R.string.tile_openai_tts),
                null,
                getString(R.string.on),
                getString(R.string.tile_google_tts),
                R.drawable.ic_tts,
                false,
                chatId,
                getString(R.string.tile_tts_desc)
            )

            tileSTT = TileFragment.newInstance(
                preferences?.getAudioModel() == "whisper",
                true,
                getString(R.string.tile_whisper_stt),
                null,getString(R.string.on),
                getString(R.string.tile_google_stt),
                R.drawable.ic_microphone,
                false,
                chatId,
                getString(R.string.tile_stt_desc)
            )
        }

        t2.start()
        t2.join()
    }

    private fun createFragments3() {
        val t3 = Thread {
            tileSilentMode = TileFragment.newInstance(
                preferences?.getSilence() == true,
                true,
                getString(R.string.tile_silent_mode_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_mute,
                preferences?.getNotSilence() == true,
                chatId,
                getString(R.string.tile_silent_mode_desc)
            )

            tileAlwaysSpeak = TileFragment.newInstance(
                preferences?.getNotSilence() == true,
                true,
                getString(R.string.tile_always_speak_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_volume_up,
                preferences?.getSilence() == true,
                chatId,
                getString(R.string.tile_always_speak_desc)
            )

            tileTextModel = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_text_model_title),
                null,
                model,
                null,
                R.drawable.chatgpt_icon,
                false,
                chatId,
                getString(R.string.tile_text_generation_model_desc)
            )

            tileActivationMessage = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_activation_prompt_title),
                null,
                getString(R.string.label_tap_to_set),
                null,
                R.drawable.ic_play,
                false,
                chatId,
                getString(R.string.tile_activation_prompt_desc)
            )

            tileSystemMessage = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_system_message_title),
                null,
                getString(R.string.label_tap_to_set),
                null,
                R.drawable.ic_play,
                false,
                chatId,
                getString(R.string.tile_system_message_desc)
            )
        }

        t3.start()
        t3.join()
    }

    private fun createFragments4() {
        val t4 = Thread {
            tileLangDetect = TileFragment.newInstance(
                preferences?.getAutoLangDetect() == true,
                true,
                getString(R.string.tile_ale_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_language,
                false,
                chatId,
                getString(R.string.tile_ale_desc)
            )

            tileChatLayout = TileFragment.newInstance(
                preferences?.getLayout() == "classic",
                true,
                getString(R.string.tile_classic_layout_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_chat,
                false,
                chatId,
                getString(R.string.tile_classic_layout_desc)
            )

            tileFunctionCalling = TileFragment.newInstance(
                preferences?.getFunctionCalling() == true,
                true,
                "Function calling",
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable function calling. Please note that this feature is experimental and unstable."
            )

            tileSlashCommands = TileFragment.newInstance(
                preferences?.getImagineCommand() == true,
                true,
                getString(R.string.tile_sh_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                false,
                chatId,
                getString(R.string.tile_sh_desc)
            )

            tileDesktopMode = TileFragment.newInstance(
                preferences?.getDesktopMode() == true,
                true,
                getString(R.string.tile_desktop_mode_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_desktop,
                false,
                chatId,
                getString(R.string.tile_desktop_mode_desc)
            )

            tileMonochromeBackgroundForChatList = TileFragment.newInstance(
                preferences?.getMonochromeBackgroundForChatList() == true,
                true,
                "Monochrome background for chat list",
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable monochrome background for chat list."
            )
        }

        t4.start()
        t4.join()
    }

    private fun createFragments5() {
        val t5 = Thread {
            tileAboutApp = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_about_app_title),
                null,
                getString(R.string.tile_about_app_subtitle),
                null,
                R.drawable.ic_info,
                false,
                chatId,
                getString(R.string.tile_about_app_desc)
            )

            tileClearChat = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_clear_chat_title),
                null,
                getString(R.string.tile_clear_chat_subtitle),
                null,
                R.drawable.ic_close,
                chatId == "",
                chatId,
                getString(R.string.tile_clear_chat_desc)
            )

            tileDocumentation = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_documentation_title),
                null,
                getString(R.string.tile_documentation_subtitle),
                null,
                R.drawable.ic_book,
                false,
                chatId,
                getString(R.string.tile_documentation_desc)
            )

            tileAmoledMode = TileFragment.newInstance(
                preferences?.getAmoledPitchBlack() == true && isDarkThemeEnabled(),
                true,
                getString(R.string.tile_amoled_mode_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                !isDarkThemeEnabled(),
                chatId,
                getString(R.string.tile_amoled_mode_desc)
            )

            tileHideModelNames = TileFragment.newInstance(
                preferences?.getHideModelNames() == true,
                true,
                "Hide model names",
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_visibility_off,
                false,
                chatId,
                "This feature allows you to hide model names in the chat list to make it more minimalist and less distractive."
            )
        }

        t5.start()
        t5.join()
    }

    private fun createFragments6() {
        val t6 = Thread {
            tileLockAssistantWindow = TileFragment.newInstance(
                preferences?.getLockAssistantWindow() == true,
                true,
                getString(R.string.tile_las_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_lock,
                false,
                chatId,
                getString(R.string.tile_las_desc)
            )

            tileCustomize = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_assistant_customize_title),
                null,
                getString(R.string.tile_assistant_customize_title),
                null,
                R.drawable.ic_experiment,
                false,
                chatId,
                getString(R.string.tile_assistant_customize_desc)
            )

            tileDeleteData = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_delete_data_title),
                null,
                getString(R.string.tile_delete_data_subtitle),
                null,
                R.drawable.ic_delete,
                false,
                chatId,
                getString(R.string.tile_delete_data_desc)
            )

            val IID = if (installationId == "00000000-0000-0000-0000-000000000000" || installationId == "") "<Not assigned>" else installationId

            val usageEnabled: Boolean = getSharedPreferences("consent", MODE_PRIVATE).getBoolean("usage", false)

            tileSendDiagnosticData = TileFragment.newInstance(
                usageEnabled,
                false,
                getString(R.string.tile_uad_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_send,
                false,
                chatId,
                "This feature allows you to manage diagnostics data.\nInstallation ID: $IID\nAndroid ID: $androidId"
            )

            tileGetNewInstallationId = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_assign_iid_title),
                null,
                getString(R.string.tile_assign_iid_title),
                null,
                R.drawable.ic_privacy,
                false,
                chatId,
                getString(R.string.tile_assign_iid_desc)
            )

            tileRevokeAuthorization = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_revoke_authorization_title),
                null,
                if (installationId == "00000000-0000-0000-0000-000000000000" || installationId == "") "Authorization revoked" else "Revoke authorization",
                null,
                R.drawable.ic_close,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                getString(R.string.tile_revoke_authorization_desc)
            )
        }

        t6.start()
        t6.join()
    }

    private fun createFragments7() {
        val t7 = Thread {
            val logView = if (installationId == "00000000-0000-0000-0000-000000000000" || installationId == "") "Authorization revoked" else "Tap to view"

            tileCrashLog = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_crash_log_title),
                null,
                logView,
                null,
                R.drawable.ic_bug,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                getString(R.string.tile_crash_log_desc)
            )

            tileEventLog = TileFragment.newInstance(
                false,
                false,
                getString(R.string.tile_events_log_title),
                null,
                logView,
                null,
                R.drawable.ic_bug,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                getString(R.string.tile_events_log_desc)
            )

            tileChatsAutoSave = TileFragment.newInstance(
                preferences?.getChatsAutosave() == true,
                true,
                getString(R.string.tile_autosave_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                false,
                chatId,
                getString(R.string.tile_autosave_desc)
            )

            tileShowChatErrors = TileFragment.newInstance(
                preferences?.showChatErrors() == true,
                true,
                getString(R.string.tile_show_chat_errors_title),
                null,
                getString(R.string.on),
                getString(R.string.off),
                R.drawable.ic_experiment,
                false,
                chatId,
                getString(R.string.tile_show_chat_errors_desc)
            )
        }

        t7.start()
        t7.join()
    }

    private fun placeFragments() : FragmentTransaction {
        val operation = supportFragmentManager.beginTransaction().replace(R.id.tile_account, tileAccountFragment!!)
            .replace(R.id.tile_assistant, tileAssistant!!)
            .replace(R.id.tile_api, tileApiKey!!)
            .replace(R.id.tile_autosend, tileAutoSend!!)
            .replace(R.id.tile_voice, tileVoice!!)
            .replace(R.id.tile_voice_language, tileVoiceLanguage!!)
            .replace(R.id.tile_image_model, tileImageModel!!)
            .replace(R.id.tile_image_resolution, tileImageResolution!!)
            .replace(R.id.tile_tts, tileTTS!!)
            .replace(R.id.tile_stt, tileSTT!!)
            .replace(R.id.tile_silent_mode, tileSilentMode!!)
            .replace(R.id.tile_always_speak, tileAlwaysSpeak!!)
            .replace(R.id.tile_text_model, tileTextModel!!)
            .replace(R.id.tile_activation_prompt, tileActivationMessage!!)
            .replace(R.id.tile_system_message, tileSystemMessage!!)
            .replace(R.id.tile_auto_language_detection, tileLangDetect!!)
            .replace(R.id.tile_chat_layout, tileChatLayout!!)
            .replace(R.id.tile_function_calling, tileFunctionCalling!!)
            .replace(R.id.tile_slash_commands, tileSlashCommands!!)
            .replace(R.id.tile_desktop_mode, tileDesktopMode!!)
            .replace(R.id.tile_amoled_mode, tileAmoledMode!!)
            .replace(R.id.tile_lock_assistant, tileLockAssistantWindow!!)
            .replace(R.id.tile_customize, tileCustomize!!)
            .replace(R.id.tile_chats_autosave, tileChatsAutoSave!!)
            .replace(R.id.tile_about_app, tileAboutApp!!)
            .replace(R.id.tile_clear_chat, tileClearChat!!)
            .replace(R.id.tile_documentation, tileDocumentation!!)
            .replace(R.id.tile_delete_data, tileDeleteData!!)
            .replace(R.id.tile_send_diagnostic_data, tileSendDiagnosticData!!)
            .replace(R.id.tile_revoke_authorization, tileRevokeAuthorization!!)
            .replace(R.id.tile_assign_new_id, tileGetNewInstallationId!!)
            .replace(R.id.tile_crash_log, tileCrashLog!!)
            .replace(R.id.tile_event_log, tileEventLog!!)
            .replace(R.id.tile_show_chat_errors, tileShowChatErrors!!)
            .replace(R.id.tile_hide_model_names, tileHideModelNames!!)
            .replace(R.id.tile_monochrome_background_for_chat_list, tileMonochromeBackgroundForChatList!!)

        return operation
    }

    private fun initTeslasoftID() {
        teslasoftIDCircledButton = supportFragmentManager.findFragmentById(R.id.teslasoft_id_btn) as TeslasoftIDCircledButton
        teslasoftIDCircledButton?.setAccountSyncListener(accountSyncListener)
    }

    private var apiEndpointActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val apiEndpointId = data?.getStringExtra("apiEndpointId")

            if (apiEndpointId != null) {
                apiEndpoint = apiEndpointPreferences?.getApiEndpoint(this, apiEndpointId)
                host = apiEndpoint?.host ?: ""
                preferences?.setApiEndpointId(apiEndpointId)
                tileApiKey?.updateSubtitle(host)
            }
        }
    }

    private fun initializeLogic() {
        btnBack?.setOnClickListener {
            finishActivity()
        }

        tileAccountFragment?.setOnTileClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://platform.openai.com/account")
            startActivity(intent)
        }

        tileApiKey?.setOnTileClickListener {
            apiEndpointActivityResultLauncher.launch(Intent(this, ApiEndpointsListActivity::class.java))
        }

        tileAutoSend?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setAutoSend(true)
            } else {
                preferences?.setAutoSend(false)
            }
        }}

        tileAssistant?.setOnTileClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        tileVoice?.setOnTileClickListener {
            val voiceSelectorDialogFragment: VoiceSelectorDialogFragment = VoiceSelectorDialogFragment.newInstance(if (ttsEngine == "google") preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: "", chatId, ttsEngine)
            voiceSelectorDialogFragment.setVoiceSelectedListener(voiceSelectorListener)
            voiceSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "VoiceSelectorDialogFragment")
        }

        tileVoiceLanguage?.setOnTileClickListener {
            val languageSelectorDialogFragment: LanguageSelectorDialogFragment = LanguageSelectorDialogFragment.newInstance(language, chatId)
            languageSelectorDialogFragment.setStateChangedListener(languageChangedListener)
            languageSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "LanguageSelectorDialog")
        }

        tileImageModel?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setDalleVersion("3")
            } else {
                preferences?.setDalleVersion("2")
            }
        }}

        tileImageResolution?.setOnTileClickListener {
            val resolutionSelectorDialogFragment: SelectResolutionFragment = SelectResolutionFragment.newInstance(resolution, chatId)
            resolutionSelectorDialogFragment.setStateChangedListener(resolutionChangedListener)
            resolutionSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "ResolutionSelectorDialog")
        }

        tileTTS?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setTtsEngine("openai")
                ttsEngine = "openai"
            } else {
                preferences?.setTtsEngine("google")
                ttsEngine = "google"
            }

            voice = if (!ischecked) preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: ""
            tileVoice?.updateSubtitle(voice)
        }}

        tileSTT?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setAudioModel("whisper")
            } else {
                preferences?.setAudioModel("google")
            }
        }}

        tileSilentMode?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setSilence(true)
                preferences?.setNotSilence(false)
                tileAlwaysSpeak?.setChecked(false)
                tileAlwaysSpeak?.setEnabled(false)
            } else {
                preferences?.setSilence(false)
                tileAlwaysSpeak?.setEnabled(true)
            }
        }}

        tileAlwaysSpeak?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setNotSilence(true)
                preferences?.setSilence(false)
                tileSilentMode?.setChecked(false)
                tileSilentMode?.setEnabled(false)
            } else {
                preferences?.setNotSilence(false)
                tileSilentMode?.setEnabled(true)
            }
        }}

        tileTextModel?.setOnTileClickListener {
            val advancedSettingsDialogFragment: AdvancedSettingsDialogFragment = AdvancedSettingsDialogFragment.newInstance(model, chatId)
            advancedSettingsDialogFragment.setStateChangedListener(modelChangedListener)
            advancedSettingsDialogFragment.show(supportFragmentManager.beginTransaction(), "AdvancedSettingsDialog")
        }

        tileActivationMessage?.setOnTileClickListener {
            val promptDialog: ActivationPromptDialogFragment = ActivationPromptDialogFragment.newInstance(activationPrompt)
            promptDialog.setStateChangedListener(promptChangedListener)
            promptDialog.show(supportFragmentManager.beginTransaction(), "PromptDialog")
        }

        tileSystemMessage?.setOnTileClickListener {
            val promptDialog: SystemMessageDialogFragment = SystemMessageDialogFragment.newInstance(systemMessage)
            promptDialog.setStateChangedListener(systemChangedListener)
            promptDialog.show(supportFragmentManager.beginTransaction(), "SystemMessageDialog")
        }

        tileLangDetect?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setAutoLangDetect(true)
            } else {
                preferences?.setAutoLangDetect(false)
            }
        }}

        tileChatLayout?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setLayout("classic")
            } else {
                preferences?.setLayout("bubbles")
            }
        }}

        tileFunctionCalling?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setFunctionCalling(true)
            } else {
                preferences?.setFunctionCalling(false)
            }
        }}

        tileSlashCommands?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setImagineCommand(true)
            } else {
                preferences?.setImagineCommand(false)
            }
        }}

        tileDesktopMode?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setDesktopMode(true)
            } else {
                preferences?.setDesktopMode(false)
            }
        }}

        tileAmoledMode?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setAmoledPitchBlack(true)
            } else {
                preferences?.setAmoledPitchBlack(false)
            }

            restartActivity()
        }}

        tileLockAssistantWindow?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setLockAssistantWindow(true)
            } else {
                preferences?.setLockAssistantWindow(false)
            }
        }}

        tileChatsAutoSave?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setChatsAutosave(true)
            } else {
                preferences?.setChatsAutosave(false)
            }
        }}

        tileHideModelNames?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setHideModelNames(true)
            } else {
                preferences?.setHideModelNames(false)
            }
        }}

        tileMonochromeBackgroundForChatList?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setMonochromeBackgroundForChatList(true)
            } else {
                preferences?.setMonochromeBackgroundForChatList(false)
            }
        }}

        tileAboutApp?.setOnTileClickListener {
            startActivity(Intent(this, AboutActivity::class.java).putExtra("chatId", chatId))
        }

        tileClearChat?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_clear_chat)
                .setMessage(R.string.msg_clear_chat)
                .setPositiveButton(R.string.yes) { _, _ ->
                    run {
                        ChatPreferences.getChatPreferences().clearChat(this, chatId)
                        Toast.makeText(this, getString(R.string.submsg_chat_cleared), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
        }

        tileDocumentation?.setOnTileClickListener {
            startActivity(Intent(this, DocumentationActivity::class.java).putExtra("chatId", chatId))
        }

        tileDeleteData?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_delete_data)
                .setMessage(R.string.msg_delete_data)
                .setPositiveButton(R.string.yes) { _, _ ->
                    run {
                        Logger.deleteAllLogs(this)
                        // TODO: Send deletion request when API will be ready
                        Toast.makeText(this, getString(R.string.submsg_data_deletion), Toast.LENGTH_SHORT).show()
                        resetDeviceId()
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
        }

        tileSendDiagnosticData?.setOnTileClickListener {
            if (getSharedPreferences("consent", MODE_PRIVATE).getBoolean("usage", false)) {
                MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                    .setTitle(R.string.label_uad)
                    .setMessage(R.string.msg_uad)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        run {
                            getSharedPreferences("consent", MODE_PRIVATE).edit().putBoolean("usage", false).apply()
                            tileSendDiagnosticData?.setChecked(false)
                            restartActivity()
                        }
                    }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .show()
            } else {
                MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                    .setTitle(R.string.label_uad_optin)
                    .setMessage(R.string.mgs_uad_optin)
                    .setPositiveButton(R.string.btn_agree_and_enable) { _, _ ->
                        run {
                            getSharedPreferences("consent", MODE_PRIVATE).edit().putBoolean("usage", true).apply()
                            DeviceInfoProvider.assignInstallationId(this)
                            tileSendDiagnosticData?.setChecked(true)
                            restartActivity()
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
            }
        }

        tileRevokeAuthorization?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_revoke_authorization)
                .setMessage("Are you sure you want to revoke authorization? After you revoke your authorization this app will stop collecting data and delete data from their servers. This action will prevent this app from writing logs (even locally). Installation ID will be removed. Once you enable usage and diagnostics this setting will be reset. This option may prevent you from bug reporting. Would you like to continue?")
                .setPositiveButton(R.string.yes) { _, _ ->
                    run {
                        DeviceInfoProvider.revokeAuthorization(this)
                        restartActivity()
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
        }

        tileGetNewInstallationId?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_iid_assign)
                .setMessage(R.string.msg_iid_assign)
                .setPositiveButton(R.string.btn_iid_assign) { _, _ ->
                    run {
                        DeviceInfoProvider.resetInstallationId(this)
                        restartActivity()
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }

        tileCrashLog?.setOnTileClickListener {
            startActivity(Intent(this, LogsActivity::class.java).putExtra("type", "crash").putExtra("chatId", chatId))
        }

        tileEventLog?.setOnTileClickListener {
            startActivity(Intent(this, LogsActivity::class.java).putExtra("type", "event").putExtra("chatId", chatId))
        }

        tileShowChatErrors?.setOnCheckedChangeListener { ischecked ->
            if (ischecked) {
                preferences?.setShowChatErrors(true)
            } else {
                preferences?.setShowChatErrors(false)
            }
        }

        tileCustomize?.setOnTileClickListener {
            val customizeAssistantDialogFragment: CustomizeAssistantDialog = CustomizeAssistantDialog.newInstance(chatId, preferences?.getAssistantName() ?: "SpeakGPT", preferences?.getAvatarType() ?: "builtin", preferences?.getAvatarId() ?: "gpt")
            customizeAssistantDialogFragment.setCustomizeAssistantDialogListener(customizeAssistantDialogListener)
            customizeAssistantDialogFragment.show(supportFragmentManager.beginTransaction(), "CustomizeAssistantDialog")
        }
    }

    private fun restartActivity() {
        runOnUiThread {
            recreate()
        }
    }

    private fun resetDeviceId() {
        MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
            .setTitle(R.string.label_iid_reset)
            .setMessage(R.string.msg_iid_reset)
            .setPositiveButton(R.string.btn_reset) { _, _ ->
                run {
                    DeviceInfoProvider.resetInstallationId(this)
                    restartActivity()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    @Suppress("DEPRECATION")
    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }

            btnBack?.setBackgroundResource(R.drawable.btn_accent_icon_large_amoled)
        } else {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }

            btnBack?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_icon_large, theme)!!)
        }
    }

    private fun isDefaultAssistantApp(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
        } else {
            // For older versions, use the Settings API to check
            val defaultAssistPackage = Settings.Secure.getString(
                context.contentResolver,
                "voice_interaction_service"
            )

            val myPackage = context.packageName
            defaultAssistPackage.contains(myPackage)
        }
    }

    override fun onResume() {
        super.onResume()

        if (areFragmentsInitialized) {
            tileAssistant?.setChecked(isDefaultAssistantApp(this))
        }

        // Reset preferences singleton
        Preferences.getPreferences(this, chatId)
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
            ResourcesCompat.getColor(resources, R.color.accent_50, theme)
        } else {
            SurfaceColors.SURFACE_5.getColor(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        WindowInsetsUtil.adjustPaddings(this, R.id.scrollable, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS), customPaddingBottom = dpToPx(48))
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private fun finishActivity() {
        val root: View = findViewById(R.id.root)
        root.animate().alpha(0.0f).setDuration(200)
        supportFinishAfterTransition()
    }
}
