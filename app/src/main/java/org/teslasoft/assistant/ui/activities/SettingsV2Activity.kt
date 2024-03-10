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

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
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
import org.teslasoft.assistant.ui.onboarding.ActivationActivity
import org.teslasoft.core.auth.client.TeslasoftIDClient
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

    private var btnBack: ImageButton? = null

    private var teslasoftIDClient: TeslasoftIDClient? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_new)

        btnBack = findViewById(R.id.btn_back)

        btnBack?.background = getDisabledDrawable(btnBack?.background!!)

        val activitySettingsTitle: TextView? = findViewById(R.id.activity_new_settings_title)

        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            if (chatId == "") {
                tileClearChat?.setEnabled(false)
                activitySettingsTitle?.text = getString(R.string.global_settings_title)
            } else {
                activitySettingsTitle?.text = getString(R.string.chat_settings_title)
            }
        } else {
            tileClearChat?.setEnabled(false)
            activitySettingsTitle?.text = getString(R.string.global_settings_title)
        }

        preferences = Preferences.getPreferences(this, chatId)

        model = preferences?.getModel() ?: "gpt-3.5-turbo"
        activationPrompt = preferences?.getPrompt() ?: ""
        systemMessage = preferences?.getSystemMessage() ?: ""
        language = preferences?.getLanguage() ?: "en"
        resolution = preferences?.getResolution() ?: "256x256"
        voice = if (preferences?.getTtsEngine() == "google") preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: ""
        host = preferences?.getCustomHost() ?: ""

        tileAccountFragment = TileFragment.newInstance(
            false,
            false,
            "Account",
            null,
            "Manage OpenAI account",
            null,
            R.drawable.ic_user,
            false
        )

        tileAssistant = TileFragment.newInstance(
            isDefaultAssistantApp(this),
            false,
            "Default assistant",
            null,
            "On",
            "Off",
            R.drawable.ic_assistant,
            false
        )

        tileApiKey = TileFragment.newInstance(
            false,
            false,
            "API key",
            null,
            "****************",
            null,
            R.drawable.ic_key,
            false
        )

        tileCustomHost = TileFragment.newInstance(
            false,
            false,
            "Custom host",
            null,
            host,
            null,
            R.drawable.ic_user,
            false
        )

        tileVoice = TileFragment.newInstance(
            false,
            false,
            "Voice",
            null,
            voice,
            null,
            R.drawable.ic_voice,
            false
        )

        tileVoiceLanguage = TileFragment.newInstance(
            false,
            false,
            "Voice language",
            null,
            Locale.forLanguageTag(preferences?.getLanguage()!!).displayLanguage,
            null,
            R.drawable.ic_language,
            false
        )

        tileImageModel = TileFragment.newInstance(
            preferences?.getDalleVersion() == "3",
            true,
            "Use DALL-e 3",
            null,
            "On",
            "DALL-e 2 is used",
            R.drawable.ic_image,
            false
        )

        tileImageResolution = TileFragment.newInstance(
            false,
            false,
            "Image resolution",
            null,
            preferences?.getResolution()!!,
            null,
            R.drawable.ic_image,
            false
        )

        tileTTS = TileFragment.newInstance(
            preferences?.getTtsEngine() == "openai",
            true,
            "Use OpenAI TTS",
            null,
            "On",
            "Google TTS is used",
            R.drawable.ic_tts,
            false
        )

        tileSTT = TileFragment.newInstance(
            preferences?.getAudioModel() == "whisper",
            true,
            "Use Whisper",
            null,
            "On",
            "Google STT is used",
            R.drawable.ic_microphone,
            false
        )

        tileSilentMode = TileFragment.newInstance(
            preferences?.getSilence() == true,
            true,
            "Silent mode",
            null,
            "On",
            "Off",
            R.drawable.ic_mute,
            preferences?.getNotSilence() == true
        )

        tileAlwaysSpeak = TileFragment.newInstance(
            preferences?.getNotSilence() == true,
            true,
            "Always speak",
            null,
            "On",
            "Off",
            R.drawable.ic_volume_up,
            preferences?.getSilence() == true
        )

        tileTextModel = TileFragment.newInstance(
            false,
            false,
            "Text model",
            null,
            model,
            null,
            R.drawable.chatgpt_icon,
            false
        )

        tileActivationMessage = TileFragment.newInstance(
            false,
            false,
            "Activation prompt",
            null,
            "Tap to set",
            null,
            R.drawable.ic_play,
            false
        )

        tileSystemMessage = TileFragment.newInstance(
            false,
            false,
            "System message",
            null,
            "Tap to set",
            null,
            R.drawable.ic_play,
            false
        )

        tileLangDetect = TileFragment.newInstance(
            preferences?.getAutoLangDetect() == true,
            true,
            "Automatic language detection",
            null,
            "On",
            "Off",
            R.drawable.ic_language,
            false
        )

        tileChatLayout = TileFragment.newInstance(
            preferences?.getLayout() == "classic",
            true,
            "Classic chat layout",
            null,
            "On",
            "Off",
            R.drawable.ic_chat,
            false
        )

        tileFunctionCalling = TileFragment.newInstance(
            preferences?.getFunctionCalling() == true,
            true,
            "Function calling",
            null,
            "On",
            "Off",
            R.drawable.ic_experiment,
            false
        )

        tileSlashCommands = TileFragment.newInstance(
            preferences?.getImagineCommand() == true,
            true,
            "Slash commands",
            null,
            "On",
            "Off",
            R.drawable.ic_experiment,
            false
        )

        tileDesktopMode = TileFragment.newInstance(
            preferences?.getDesktopMode() == true,
            true,
            "Desktop mode",
            null,
            "On",
            "Off",
            R.drawable.ic_desktop,
            false
        )

        tileNewLook = TileFragment.newInstance(
            preferences?.getExperimentalUI() == true,
            true,
            "New UI",
            null,
            "On",
            "Off",
            R.drawable.ic_experiment,
            false
        )

        tileAboutApp = TileFragment.newInstance(
            false,
            false,
            "About app",
            null,
            "About SpeakGPT",
            null,
            R.drawable.ic_info,
            false
        )

        tileClearChat = TileFragment.newInstance(
            false,
            false,
            "Clear chat",
            null,
            "Clear chat history",
            null,
            R.drawable.ic_close,
            chatId == ""
        )

        tileDocumentation = TileFragment.newInstance(
            false,
            false,
            "Documentation",
            null,
            "Open documentation",
            null,
            R.drawable.ic_book,
            false
        )

        supportFragmentManager.beginTransaction().replace(R.id.tile_account, tileAccountFragment!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_assistant, tileAssistant!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_api, tileApiKey!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_host, tileCustomHost!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_voice, tileVoice!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_voice_language, tileVoiceLanguage!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_image_model, tileImageModel!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_image_resolution, tileImageResolution!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_tts, tileTTS!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_stt, tileSTT!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_silent_mode, tileSilentMode!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_always_speak, tileAlwaysSpeak!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_text_model, tileTextModel!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_activation_prompt, tileActivationMessage!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_system_message, tileSystemMessage!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_auto_language_detection, tileLangDetect!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_chat_layout, tileChatLayout!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_function_calling, tileFunctionCalling!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_slash_commands, tileSlashCommands!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_desktop_mode, tileDesktopMode!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_new_look, tileNewLook!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_about_app, tileAboutApp!!).commit()
        supportFragmentManager.beginTransaction().replace(R.id.tile_clear_chat, tileClearChat!!).commit()

        supportFragmentManager.beginTransaction().replace(R.id.tile_documentation, tileDocumentation!!).commit()

        initializeLogic()
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
            val voiceSelectorDialogFragment: VoiceSelectorDialogFragment = VoiceSelectorDialogFragment.newInstance("")
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
            } else {
                preferences?.setTtsEngine("google")
            }
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
                preferences?.setLayout("modern")
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

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return SurfaceColors.SURFACE_2.getColor(this)
    }
}