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

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Logger
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.TileFragment
import org.teslasoft.assistant.ui.fragments.dialogs.ActivationPromptDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.AdvancedSettingsDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.ApiKeyDialog
import org.teslasoft.assistant.ui.fragments.dialogs.HostnameEditorDialog
import org.teslasoft.assistant.ui.fragments.dialogs.LanguageSelectorDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.SelectResolutionFragment
import org.teslasoft.assistant.ui.fragments.dialogs.SystemMessageDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.VoiceSelectorDialogFragment
import org.teslasoft.core.auth.AccountSyncListener
import org.teslasoft.core.auth.client.SettingsListener
import org.teslasoft.core.auth.client.SyncListener
import org.teslasoft.core.auth.client.TeslasoftIDClient
import org.teslasoft.core.auth.widget.TeslasoftIDCircledButton
import java.io.IOException
import java.util.Locale


class SettingsV2Activity : FragmentActivity() {

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

    private var tileAccountFragment: TileFragment? = null
    private var tileAssistant: TileFragment? = null
    private var tileApiKey: TileFragment? = null
    private var tileCustomHost: TileFragment? = null
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
    private var tileNewLook: TileFragment? = null
    private var tileAboutApp: TileFragment? = null
    private var tileClearChat: TileFragment? = null
    private var tileDocumentation: TileFragment? = null
    private var tileAmoledMode: TileFragment? = null
    private var tileLockAssistantWindow: TileFragment? = null
    private var tileDeleteData: TileFragment? = null
    private var tileSendDiagnosticData: TileFragment? = null
    private var tileRevokeAuthorization: TileFragment? = null
    private var tileGetNewInstallationId: TileFragment? = null
    private var tileCrashLog: TileFragment? = null
    private var tileAdsLog: TileFragment? = null
    private var tileEventLog: TileFragment? = null
    private var tileDebugTestAds: TileFragment? = null
    private var tileChatsAutoSave: TileFragment? = null
    private var threadLoading: LinearLayout? = null

    private var teslasoftIDCircledButton: TeslasoftIDCircledButton? = null

    private var adId = "<Loading...>"
    private var installationId = ""

    private var root: ScrollView? = null
    private var textGlobal: TextView? = null

    private var ad: LinearLayout? = null

    private var btnBack: ImageButton? = null

    private var teslasoftIDClient: TeslasoftIDClient? = null

    private var testAdsReady: Boolean = false

    private var areFragmentsInitialized = false

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
            if (name == "") Toast.makeText(this@SettingsV2Activity, "Error, no model name is provided", Toast.LENGTH_SHORT).show()
            else if (name.contains("gpt-4")) Toast.makeText(this@SettingsV2Activity, "Error, GPT4 support maximum of 8192 tokens", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this@SettingsV2Activity, "Error, more than 2048 tokens is not supported", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@SettingsV2Activity, "Please select language", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@SettingsV2Activity, "Please select resolution", Toast.LENGTH_SHORT).show()
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
            this@SettingsV2Activity.voice = voice

            tileVoice?.updateSubtitle(voice)
        }

    private var hostChangedListener: HostnameEditorDialog.StateChangesListener = object : HostnameEditorDialog.StateChangesListener {
        override fun onFormError(name: String) {
            runOnUiThread {
                Toast.makeText(this@SettingsV2Activity, "Please enter hostname", Toast.LENGTH_SHORT).show()
            }

            val hostnameEditorDialog: HostnameEditorDialog = HostnameEditorDialog.newInstance(name)
            hostnameEditorDialog.setStateChangedListener(this)
            hostnameEditorDialog.show(supportFragmentManager.beginTransaction(), "HostEditorDialog")
        }

        override fun onSelected(name: String) {
            host = name
            tileCustomHost?.updateSubtitle(name)
            preferences?.setCustomHost(name)
        }
    }

    private var apiChangedListener: ApiKeyDialog.StateChangesListener = object : ApiKeyDialog.StateChangesListener {
        override fun onSelected(name: String) {
            preferences?.setApiKey(name, this@SettingsV2Activity)
        }

        override fun onFormError(name: String) {
            Toast.makeText(this@SettingsV2Activity, "Please enter API key", Toast.LENGTH_SHORT).show()
            val apiKeyDialog: ApiKeyDialog = ApiKeyDialog.newInstance(name)
            apiKeyDialog.setStateChangedListener(this)
            apiKeyDialog.show(supportFragmentManager.beginTransaction(), "ApiKeyDialog")
        }
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
                        if (areFragmentsInitialized) {
                            tileDebugTestAds?.setEnabled(true)
                        }

                        testAdsReady = true
                    } else {
                        preferences?.setDebugMode(false)
                        if (areFragmentsInitialized) {
                            tileDebugTestAds?.setEnabled(false)
                        }

                        preferences?.setDebugTestAds(false)
                        testAdsReady = false
                    }
                }
            }.start()
        }

        override fun onAuthFailed(state: String, message: String) {
            runOnUiThread {
                preferences?.setDebugMode(false)
                if (areFragmentsInitialized) {
                    tileDebugTestAds?.setEnabled(false)
                }

                preferences?.setDebugTestAds(false)
                testAdsReady = false
            }
        }

        override fun onSignedOut() {
            runOnUiThread {
                preferences?.setDebugMode(false)
                if (areFragmentsInitialized) {
                    tileDebugTestAds?.setEnabled(false)
                }

                preferences?.setDebugTestAds(false)
                testAdsReady = false
                recreate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_new)

        btnBack = findViewById(R.id.btn_back)
        root = findViewById(R.id.root)
        textGlobal = findViewById(R.id.text_global)
        threadLoading = findViewById(R.id.thread_loading)

        threadLoading?.visibility = View.VISIBLE

        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            if (chatId == "") {
                tileClearChat?.setEnabled(false)
                textGlobal?.visibility = TextView.VISIBLE
            } else {
                textGlobal?.visibility = TextView.GONE
            }
        } else {
            tileClearChat?.setEnabled(false)
            textGlobal?.visibility = TextView.VISIBLE
        }

        preferences = Preferences.getPreferences(this, chatId)

        model = preferences?.getModel() ?: "gpt-3.5-turbo"
        activationPrompt = preferences?.getPrompt() ?: ""
        systemMessage = preferences?.getSystemMessage() ?: ""
        language = preferences?.getLanguage() ?: "en"
        resolution = preferences?.getResolution() ?: "256x256"
        voice = if (preferences?.getTtsEngine() == "google") preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: ""
        host = preferences?.getCustomHost() ?: ""
        ttsEngine = preferences?.getTtsEngine() ?: "google"

        initTeslasoftID()

        reloadAmoled()

        teslasoftIDClient = TeslasoftIDClient(this, "B7:9F:CB:D0:5C:69:1D:C7:DD:5C:36:50:64:1E:9B:32:00:CA:11:41:47:ED:F1:D9:64:86:2A:CA:49:CD:65:25", "d07985975904997990790c2e5088372a", "org.teslasoft.assistant", settingsListener, syncListener)

        val t1 = Thread {
            createFragments1()
            createFragments2()
            createFragments3()
            createFragments4()
            createFragments5()
        }

        val t2 = Thread {
            installationId = DeviceInfoProvider.getInstallationId(this@SettingsV2Activity)
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

                initAds()
                initializeLogic()

                Handler(Looper.getMainLooper()).postDelayed({
                    val fadeOut: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    threadLoading?.startAnimation(fadeOut)

                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                        override fun onAnimationEnd(animation: Animation) {
                            runOnUiThread {
                                threadLoading?.visibility = View.GONE
                                threadLoading?.elevation = 0.0f
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
                    })
                }, 50)
            }
        }.start()
    }

    private fun createFragments1() {
        val t1 = Thread {
            tileAccountFragment = TileFragment.newInstance(
                false,
                false,
                "Account",
                null,
                "Manage OpenAI account",
                null,
                R.drawable.ic_user,
                false,
                chatId,
                "This feature allows you to manage your OpenAI account."
            )

            tileAssistant = TileFragment.newInstance(
                isDefaultAssistantApp(this@SettingsV2Activity),
                false,
                "Default assistant",
                null,
                "On",
                "Off",
                R.drawable.ic_assistant,
                false,
                chatId,
                "This feature allows you to set SpeakGPT as your default assistant."
            )

            tileApiKey = TileFragment.newInstance(
                false,
                false,
                "API key",
                null,
                "****************",
                null,
                R.drawable.ic_key,
                false,
                chatId,
                "This feature allows you to set your OpenAI API key."
            )

            tileCustomHost = TileFragment.newInstance(
                false,
                false,
                "Custom host",
                null,
                host,
                null,
                R.drawable.ic_user,
                false,
                chatId,
                "This feature allows you to set custom OpenAI API host."
            )

            tileVoice = TileFragment.newInstance(
                false,
                false,
                "Voice",
                null,
                voice,
                null,
                R.drawable.ic_voice,
                false,
                chatId,
                "This feature allows you to set your TTS voice."
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
                "Voice language",
                null,
                Locale.forLanguageTag(preferences?.getLanguage()!!).displayLanguage,
                null,
                R.drawable.ic_language,
                false,
                chatId,
                "This feature allows you to set your TTS voice language."
            )

            tileImageModel = TileFragment.newInstance(
                preferences?.getDalleVersion() == "3",
                true,
                "Use DALL-e 3",
                null,
                "On",
                "DALL-e 2 is used",
                R.drawable.ic_image,
                false,
                chatId,
                "This feature allows you to set image generation model."
            )

            tileImageResolution = TileFragment.newInstance(
                false,
                false,
                "Image resolution",
                null,
                preferences?.getResolution()!!,
                null,
                R.drawable.ic_image,
                false,
                chatId,
                "This feature allows you to set image resolution. Please note that DALL-e 3 supports only 1024x1024."
            )

            tileTTS = TileFragment.newInstance(
                preferences?.getTtsEngine() == "openai",
                true,
                "Use OpenAI TTS",
                null,
                "On",
                "Google TTS is used",
                R.drawable.ic_tts,
                false,
                chatId,
                "This feature allows you to set TTS engine. OpenAI TTS is paid."
            )

            tileSTT = TileFragment.newInstance(
                preferences?.getAudioModel() == "whisper",
                true,
                "Use Whisper",
                null,
                "On",
                "Google STT is used",
                R.drawable.ic_microphone,
                false,
                chatId,
                "This feature allows you to set STT engine. Whisper is paid."
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
                "Silent mode",
                null,
                "On",
                "Off",
                R.drawable.ic_mute,
                preferences?.getNotSilence() == true,
                chatId,
                "This feature allows you let SpeakGPT never pronounce answers."
            )

            tileAlwaysSpeak = TileFragment.newInstance(
                preferences?.getNotSilence() == true,
                true,
                "Always speak",
                null,
                "On",
                "Off",
                R.drawable.ic_volume_up,
                preferences?.getSilence() == true,
                chatId,
                "This feature allows you let SpeakGPT always pronounce answers."
            )

            tileTextModel = TileFragment.newInstance(
                false,
                false,
                "Text model",
                null,
                model,
                null,
                R.drawable.chatgpt_icon,
                false,
                chatId,
                "This feature allows you to set text generation model as well as model's params."
            )

            tileActivationMessage = TileFragment.newInstance(
                false,
                false,
                "Activation prompt",
                null,
                "Tap to set",
                null,
                R.drawable.ic_play,
                false,
                chatId,
                "This feature allows you to set activation prompt which will be sent as a first user message."
            )

            tileSystemMessage = TileFragment.newInstance(
                false,
                false,
                "System message",
                null,
                "Tap to set",
                null,
                R.drawable.ic_play,
                false,
                chatId,
                "This feature allows you to set system message which will be sent as a system message."
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
                "Automatic language detection",
                null,
                "On",
                "Off",
                R.drawable.ic_language,
                false,
                chatId,
                "This feature allows you to enable automatic language detection of speech."
            )

            tileChatLayout = TileFragment.newInstance(
                preferences?.getLayout() == "classic",
                true,
                "Classic chat layout",
                null,
                "On",
                "Off",
                R.drawable.ic_chat,
                false,
                chatId,
                "This feature allows you to set classic chat layout."
            )

            tileFunctionCalling = TileFragment.newInstance(
                preferences?.getFunctionCalling() == true,
                true,
                "Function calling",
                null,
                "On",
                "Off",
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable function calling. Please note that this feature is experimental and unstable."
            )

            tileSlashCommands = TileFragment.newInstance(
                preferences?.getImagineCommand() == true,
                true,
                "Slash commands",
                null,
                "On",
                "Off",
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable slash commands. Please note that this feature is experimental and unstable. Example: /imagine a cat."
            )

            tileDesktopMode = TileFragment.newInstance(
                preferences?.getDesktopMode() == true,
                true,
                "Desktop mode",
                null,
                "On",
                "Off",
                R.drawable.ic_desktop,
                false,
                chatId,
                "This feature allows you to enable desktop mode. When desktop mode is enabled, SpeakGPT will use keyboard shortcuts and message input will be automatically focused. Press 'Enter' to send message, 'Shift + Enter' to add new line and 'Shift+Esc' to close chat. Not recommended to use on phones unless you want keyboard to be opened every time you open the chat."
            )
        }

        t4.start()
        t4.join()
    }

    private fun createFragments5() {
        val t5 = Thread {
            tileNewLook = TileFragment.newInstance(
                preferences?.getExperimentalUI() == true,
                true,
                "New UI",
                null,
                "On",
                "Off",
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable new experimental UI. New UI is more compact and intuitive."
            )

            tileAboutApp = TileFragment.newInstance(
                false,
                false,
                "About app",
                null,
                "About SpeakGPT",
                null,
                R.drawable.ic_info,
                false,
                chatId,
                "This feature allows you to open about SpeakGPT page."
            )

            tileClearChat = TileFragment.newInstance(
                false,
                false,
                "Clear chat",
                null,
                "Clear chat history",
                null,
                R.drawable.ic_close,
                chatId == "",
                chatId,
                "This feature allows you to clear chat history."
            )

            tileDocumentation = TileFragment.newInstance(
                false,
                false,
                "Documentation",
                null,
                "Open documentation",
                null,
                R.drawable.ic_book,
                false,
                chatId,
                "This feature allows you to open SpeakGPT documentation."
            )

            tileAmoledMode = TileFragment.newInstance(
                preferences?.getAmoledPitchBlack() == true && isDarkThemeEnabled(),
                true,
                "AMOLED mode",
                null,
                "On",
                "Off",
                R.drawable.ic_experiment,
                !isDarkThemeEnabled(),
                chatId,
                "This feature allows you to enable AMOLED mode for supported displays. It can significantly save battery. AMOLED mode is only available in dark theme."
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
                "Lock assistant window",
                null,
                "On",
                "Off",
                R.drawable.ic_lock,
                false,
                chatId,
                "This feature allows you to lock assistant window. When assistant window is locked, it can not be closed by pressing back button or swiping window down. But you can use 'exit' button. This feature can prevent accidental closing of assistant window and losing unsaved conversation."
            )

            tileDeleteData = TileFragment.newInstance(
                false,
                false,
                "Delete data",
                null,
                "Delete diagnostics data",
                null,
                R.drawable.ic_delete,
                false,
                chatId,
                "This feature allows you to delete all diagnostics data collected by SpeakGPT. This action can not be undone."
            )

            val IID = if (installationId == "00000000-0000-0000-0000-000000000000" || installationId == "") "<Not assigned>" else installationId

            val usageEnabled: Boolean = getSharedPreferences("consent", MODE_PRIVATE).getBoolean("usage", false)

            tileSendDiagnosticData = TileFragment.newInstance(
                usageEnabled,
                false,
                "Usage and diagnostics",
                "Usage and diagnostics",
                "On",
                "Off",
                R.drawable.ic_send,
                false,
                chatId,
                "This feature allows you to manage diagnostics data. Installation ID: $IID."
            )

            tileGetNewInstallationId = TileFragment.newInstance(
                false,
                false,
                "Get new installation ID",
                null,
                "Get new installation ID",
                null,
                R.drawable.ic_privacy,
                false,
                chatId,
                "This feature allows you to get new installation ID."
            )

            tileRevokeAuthorization = TileFragment.newInstance(
                false,
                false,
                "Revoke authorization",
                null,
                if (installationId == "00000000-0000-0000-0000-000000000000" || installationId == "") "Authorization revoked" else "Revoke authorization",
                null,
                R.drawable.ic_close,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                "This feature allows you to revoke authorization. After you revoke your authorization this app will stop collecting data and delete data from their servers. Logging will be disabled so it will be harder to find and report bugs."
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
                "Crash log",
                "Crash log",
                logView,
                logView,
                R.drawable.ic_bug,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                "This feature allows you to view crash log."
            )

            tileAdsLog = TileFragment.newInstance(
                false,
                false,
                "Ads log",
                "Ads log",
                logView,
                logView,
                R.drawable.ic_bug,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                "This feature allows you to view ads log. Usually you don't need to use this feature."
            )

            tileEventLog = TileFragment.newInstance(
                false,
                false,
                "Event log",
                "Event log",
                logView,
                logView,
                R.drawable.ic_bug,
                installationId == "00000000-0000-0000-0000-000000000000" || installationId == "",
                chatId,
                "This feature allows you to view event log. We do not collect this log as it may contain sensitive information. Use this log if you want to report a bug."
            )

            tileDebugTestAds = TileFragment.newInstance(
                preferences?.getDebugTestAds()!!,
                true,
                "debug.test.ads",
                "debug.test.ads",
                "On",
                "Off",
                R.drawable.ic_bug,
                !testAdsReady,
                chatId,
                "This feature allows you to enable test ads. Use this feature while development process to avoid ads policy violations. Available only for Teslasoft ID accounts with dev permissions. Ads ID: $adId"
            )

            tileChatsAutoSave = TileFragment.newInstance(
                preferences?.getChatsAutosave() == true,
                true,
                "Auto-save chats",
                null,
                "On",
                "Off",
                R.drawable.ic_experiment,
                false,
                chatId,
                "This feature allows you to enable auto-save chats in assistant window. When auto-save chats is enabled, SpeakGPT will save all chats to the device storage."
            )
        }

        t7.start()
        t7.join()
    }

    private fun placeFragments() : FragmentTransaction {
        val operation = supportFragmentManager.beginTransaction().replace(R.id.tile_account, tileAccountFragment!!)
            .replace(R.id.tile_assistant, tileAssistant!!)
            .replace(R.id.tile_api, tileApiKey!!)
            .replace(R.id.tile_host, tileCustomHost!!)
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
            .replace(R.id.tile_new_look, tileNewLook!!)
            .replace(R.id.tile_amoled_mode, tileAmoledMode!!)
            .replace(R.id.tile_lock_assistant, tileLockAssistantWindow!!)
            .replace(R.id.tile_chats_autosave, tileChatsAutoSave!!)
            .replace(R.id.tile_about_app, tileAboutApp!!)
            .replace(R.id.tile_clear_chat, tileClearChat!!)
            .replace(R.id.tile_documentation, tileDocumentation!!)
            .replace(R.id.tile_delete_data, tileDeleteData!!)
            .replace(R.id.tile_send_diagnostic_data, tileSendDiagnosticData!!)
            .replace(R.id.tile_revoke_authorization, tileRevokeAuthorization!!)
            .replace(R.id.tile_assign_new_id, tileGetNewInstallationId!!)
            .replace(R.id.tile_crash_log, tileCrashLog!!)
            .replace(R.id.tile_ads_log, tileAdsLog!!)
            .replace(R.id.tile_event_log, tileEventLog!!)
            .replace(R.id.tile_debug_test_ads, tileDebugTestAds!!)

        return operation
    }

    private fun initTeslasoftID() {
        teslasoftIDCircledButton = supportFragmentManager.findFragmentById(R.id.teslasoft_id_btn) as TeslasoftIDCircledButton
        teslasoftIDCircledButton?.setAccountSyncListener(accountSyncListener)
    }

    private fun initAds() {
        val crearEventoHilo: Thread = object : Thread() {
            @SuppressLint("HardwareIds")
            override fun run() {
                val info: AdvertisingIdClient.Info?

                adId = try {
                    info = AdvertisingIdClient.getAdvertisingIdInfo(this@SettingsV2Activity)
                    info.id.toString()
                } catch (e: IOException) {
                    e.printStackTrace()
                    "<Google Play Services error>"
                } catch (e : GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                    "<Google Play Services not found>"
                } catch (e : IllegalStateException) {
                    e.printStackTrace()
                    "<IllegalStateException: ${e.message}>"
                } catch (e : GooglePlayServicesRepairableException) {
                    e.printStackTrace()
                    "<Google Play Services error>"
                }

                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                tileDebugTestAds?.setFunctionDesc("This feature allows you to enable test ads. Use this feature while development process to avoid ads policy violations. Available only for Teslasoft ID accounts with dev permissions. Ads ID: $adId\nAndroid ID: $androidId")
            }
        }
        crearEventoHilo.start()

        Thread {
            while (!areFragmentsInitialized) {
                Thread.sleep(100)
            }

            runOnUiThread {
                if (testAdsReady) {
                    tileDebugTestAds?.setEnabled(true)
                }
            }
        }.start()

        MobileAds.initialize(this) { /* unused */ }

        val testDevices: MutableList<String> = ArrayList()
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
        testDevices.add("10e46e1d-ccaa-4909-85bf-83994b920a9c")
        testDevices.add("c29eb9ca-6008-421f-b306-c559d96ea303")
        testDevices.add("5e03e1ee-7eb9-4b51-9d21-10ca1ad1abe1")
        testDevices.add("9AB1B18F59CF84AA")
        testDevices.add("27583FCB662C9F6D")

        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDevices)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        ad = findViewById(R.id.ad)

        val adView = AdView(this)
        adView.setAdSize(AdSize.LARGE_BANNER)
        adView.adUnitId = if (preferences?.getDebugTestAds()!!) "ca-app-pub-3940256099942544/9214589741" else "ca-app-pub-7410382345282120/1474294730"

        ad?.addView(adView)

        val adRequest: AdRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)

        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                ad?.visibility = View.GONE

                // Toast.makeText(this@SettingsV2Activity, "Ad failed to load with error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded() {
                ad?.visibility = View.VISIBLE
            }
        }
    }

    private fun initializeLogic() {
        btnBack?.setOnClickListener {
            finish()
        }

        tileAccountFragment?.setOnTileClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://platform.openai.com/account")
            startActivity(intent)
        }

        tileApiKey?.setOnTileClickListener {
            val apiKeyDialog: ApiKeyDialog = ApiKeyDialog.newInstance("")
            apiKeyDialog.setStateChangedListener(apiChangedListener)
            apiKeyDialog.show(supportFragmentManager.beginTransaction(), "ApiKeyDialog")
        }

        tileCustomHost?.setOnTileClickListener {
            val hostnameEditorDialog: HostnameEditorDialog = HostnameEditorDialog.newInstance(preferences?.getCustomHost()!!)
            hostnameEditorDialog.setStateChangedListener(hostChangedListener)
            hostnameEditorDialog.show(supportFragmentManager.beginTransaction(), "HostEditorDialog")
        }

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

        tileNewLook?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setExperimentalUI(true)
            } else {
                preferences?.setExperimentalUI(false)
            }
        }}

        tileAmoledMode?.setOnCheckedChangeListener { ischecked -> run {
            if (ischecked) {
                preferences?.setAmoledPitchBlack(true)
            } else {
                preferences?.setAmoledPitchBlack(false)
            }

            recreate()
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

        tileAboutApp?.setOnTileClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        tileClearChat?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Confirm")
                .setMessage("Are you sure? This action can not be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    run {
                        ChatPreferences.getChatPreferences().clearChat(this, chatId)
                        Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        tileDocumentation?.setOnTileClickListener {
            startActivity(Intent(this, DocumentationActivity::class.java))
        }

        tileDeleteData?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Delete data")
                .setMessage("Are you sure you want to delete your data? After data deletion request is sent all logs will be deleted and all analytics data sent to us will be deleted from our servers.")
                .setPositiveButton("Delete") { _, _ ->
                    run {
                        Logger.deleteAllLogs(this)
                        // TODO: Send deletion request when API will be ready
                        Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()
                        resetDeviceId()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        tileSendDiagnosticData?.setOnTileClickListener {
            if (getSharedPreferences("consent", MODE_PRIVATE).getBoolean("usage", false)) {
                MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                    .setTitle("Usage and diagnostics")
                    .setMessage("Would you like to opt out from usage and diagnostics data collection? This data is used to improve SpeakGPT and fix bugs. Please note that SpeakGPT may still create logs on your device. Logs are saved locally. If you want to prevent this app from writing logs, please revoke authorization.")
                    .setPositiveButton("Opt out") { _, _ ->
                        run {
                            getSharedPreferences("consent", MODE_PRIVATE).edit().putBoolean("usage", false).apply()
                            tileSendDiagnosticData?.setChecked(false)
                            recreate()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            } else {
                MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                    .setTitle("Usage and diagnostics")
                    .setMessage("Would you like to enable usage and diagnostics data collection? We may collect crash logs, usage info, events in order to improve SpeakGPT and fix bugs.")
                    .setPositiveButton("Agree and enable") { _, _ ->
                        run {
                            getSharedPreferences("consent", MODE_PRIVATE).edit().putBoolean("usage", true).apply()
                            DeviceInfoProvider.assignInstallationId(this)
                            tileSendDiagnosticData?.setChecked(true)
                            recreate()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            }
        }

        tileRevokeAuthorization?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Revoke authorization")
                .setMessage("Are you sure you want to revoke authorization? After you revoke your authorization this app will stop collecting data and delete data from their servers. This action will prevent this app from writing logs (even locally). Installation ID will be removed. Once you enable usage and diagnostics this setting will be reset. This option may prevent you from bug reporting.")
                .setPositiveButton("Revoke") { _, _ ->
                    run {
                        DeviceInfoProvider.revokeAuthorization(this)
                        recreate()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        tileGetNewInstallationId?.setOnTileClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Get new installation ID")
                .setMessage("After this action a new installation ID will be assigned. Authorization will be granted and SpeakGPT will continue to write logs. Logs will not be sent to us unless 'Usage and diagnostics' option is enabled. Are you sure you want to continue?")
                .setPositiveButton("Get new ID") { _, _ ->
                    run {
                        DeviceInfoProvider.resetInstallationId(this)
                        recreate()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        tileCrashLog?.setOnTileClickListener {
            startActivity(Intent(this, LogsActivity::class.java).putExtra("type", "crash"))
        }

        tileAdsLog?.setOnTileClickListener {
            startActivity(Intent(this, LogsActivity::class.java).putExtra("type", "ads"))
        }

        tileEventLog?.setOnTileClickListener {
            startActivity(Intent(this, LogsActivity::class.java).putExtra("type", "event"))
        }

        tileDebugTestAds?.setOnCheckedChangeListener { ischecked ->
            if (ischecked) {
                preferences?.setDebugTestAds(true)
            } else {
                preferences?.setDebugTestAds(false)
            }

            recreate()
        }
    }

    private fun resetDeviceId() {
        MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
            .setTitle("Reset installation ID?")
            .setMessage("Would you like to reset installation ID additionally? After you do this all info we collected will no longer be associated with your device.")
            .setPositiveButton("Reset") { _, _ ->
                run {
                    DeviceInfoProvider.resetInstallationId(this)
                    recreate()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
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

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            btnBack?.setBackgroundResource(R.drawable.btn_accent_icon_large_amoled)
            threadLoading?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
        } else {
            window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.setBackgroundDrawableResource(R.color.window_background)
            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            btnBack?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_icon_large, theme)!!)
            threadLoading?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
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
}