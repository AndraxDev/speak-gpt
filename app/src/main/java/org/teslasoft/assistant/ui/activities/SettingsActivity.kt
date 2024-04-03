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

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.materialswitch.MaterialSwitch

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.debug.DebugActivity
import org.teslasoft.assistant.ui.fragments.dialogs.ActivationPromptDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.AdvancedSettingsDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.ApiKeyDialog
import org.teslasoft.assistant.ui.fragments.dialogs.HostnameEditorDialog
import org.teslasoft.assistant.ui.fragments.dialogs.LanguageSelectorDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.SystemMessageDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.VoiceSelectorDialogFragment
import org.teslasoft.assistant.ui.onboarding.ActivationActivity
import org.teslasoft.assistant.util.TestDevicesAds
import org.teslasoft.core.auth.client.TeslasoftIDClient

class SettingsActivity : FragmentActivity() {

    private var btnChangeApi: LinearLayout? = null
    private var btnChangeAccount: LinearLayout? = null
    private var btnCustomHost: LinearLayout? = null
    private var btnSetAssistant: LinearLayout? = null
    private var silenceSwitch: MaterialSwitch? = null
    private var alwaysSpeak: MaterialSwitch? = null
    private var autoLangDetectSwitch: MaterialSwitch? = null
    private var functionCallingSwitch: MaterialSwitch? = null
    private var desktopMode: MaterialSwitch? = null
    private var imagineSwitch: MaterialSwitch? = null
    private var btnClearChat: MaterialButton? = null
    private var btnDebugMenu: MaterialButton? = null
    private var dalleResolutions: MaterialButtonToggleGroup? = null
    private var btnModelGroup: MaterialButtonToggleGroup? = null
    private var btnModel: LinearLayout? = null
    private var btnPrompt: LinearLayout? = null
    private var btnSystem: LinearLayout? = null
    private var btnAbout: LinearLayout? = null
    private var btnDalle2: MaterialButton? = null
    private var btnDalle3: MaterialButton? = null
    private var r256: MaterialButton? = null
    private var r512: MaterialButton? = null
    private var r1024: MaterialButton? = null
    private var audioGoogle: MaterialButton? = null
    private var audioWhisper: MaterialButton? = null
    private var gpt30: MaterialButton? = null
    private var gpt40: MaterialButton? = null
    private var promptDesc: TextView? = null
    private var modelDesc: TextView? = null
    private var btnClassicView: LinearLayout? = null
    private var btnBubblesView: LinearLayout? = null
    private var assistantLanguage: LinearLayout? = null
    private var btnAutoLanguageDetect: LinearLayout? = null
    private var btnVoiceSelector: LinearLayout? = null
    private var activitySettingsTitle: TextView? = null
    private var globalSettingsTip: LinearLayout? = null
    private var btnGoogleTTS: MaterialButton? = null
    private var btnOpenAITTS: MaterialButton? = null
    private var btnExperiments: ImageButton? = null
    private var btnBack: ImageButton? = null
    private var ad: LinearLayout? = null

    private var preferences: Preferences? = null
    private var chatId = ""
    private var model = ""
    private var activationPrompt = ""
    private var systemMessage = ""
    private var language = "en"
    private var ttsEngine = "google"

    private var teslasoftIDClient: TeslasoftIDClient? = null

    private var modelChangedListener: AdvancedSettingsDialogFragment.StateChangesListener = object : AdvancedSettingsDialogFragment.StateChangesListener {
        override fun onSelected(name: String, maxTokens: String, endSeparator: String, prefix: String) {
            model = name
            preferences?.setModel(name)
            preferences?.setMaxTokens(maxTokens.toInt())
            preferences?.setEndSeparator(endSeparator)
            preferences?.setPrefix(prefix)

            btnModelGroup?.isSingleSelection = false
            gpt30?.isChecked = false
            gpt40?.isChecked = false
            btnModelGroup?.isSingleSelection = true
            gpt30?.isChecked = model == "gpt-3.5-turbo"
            gpt40?.isChecked = model == "gpt-4"
        }

        override fun onFormError(name: String, maxTokens: String, endSeparator: String, prefix: String) {
            if (name == "") Toast.makeText(this@SettingsActivity, "Error, no model name is provided", Toast.LENGTH_SHORT).show()
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
        }

        override fun onFormError(name: String) {
            Toast.makeText(this@SettingsActivity, "Please select language", Toast.LENGTH_SHORT).show()
            val languageSelectorDialogFragment: LanguageSelectorDialogFragment = LanguageSelectorDialogFragment.newInstance(name, chatId)
            languageSelectorDialogFragment.setStateChangedListener(this)
            languageSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "LanguageSelectorDialog")
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

    private var hostChangedListener: HostnameEditorDialog.StateChangesListener = object : HostnameEditorDialog.StateChangesListener {
        override fun onFormError(name: String) {
            runOnUiThread {
                Toast.makeText(this@SettingsActivity, "Please enter hostname", Toast.LENGTH_SHORT).show()
            }
            val hostnameEditorDialog: HostnameEditorDialog = HostnameEditorDialog.newInstance(name)
            hostnameEditorDialog.setStateChangedListener(this)
            hostnameEditorDialog.show(supportFragmentManager.beginTransaction(), "HostEditorDialog")
        }

        override fun onSelected(name: String) {
            preferences?.setCustomHost(name)
        }
    }

    private var apiChangedListener: ApiKeyDialog.StateChangesListener = object : ApiKeyDialog.StateChangesListener {
        override fun onSelected(name: String) {
            preferences?.setApiKey(name, this@SettingsActivity)
        }

        override fun onFormError(name: String) {
            Toast.makeText(this@SettingsActivity, "Please enter API key", Toast.LENGTH_SHORT).show()
            val apiKeyDialog: ApiKeyDialog = ApiKeyDialog.newInstance(name)
            apiKeyDialog.setStateChangedListener(this)
            apiKeyDialog.show(supportFragmentManager.beginTransaction(), "ApiKeyDialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        Thread {
            runOnUiThread {
                initUI()
            }
        }.start()
    }

    private fun initUI() {
        activitySettingsTitle = findViewById(R.id.activity_settings_title)
        globalSettingsTip = findViewById(R.id.global_settings_tip)
        btnChangeApi = findViewById(R.id.btn_manage_api)
        btnChangeAccount = findViewById(R.id.btn_manage_account)
        btnCustomHost = findViewById(R.id.btn_manage_host)
        btnSetAssistant = findViewById(R.id.btn_manage_assistant)
        silenceSwitch = findViewById(R.id.silent_switch)
        alwaysSpeak = findViewById(R.id.always_speak_switch)
        autoLangDetectSwitch = findViewById(R.id.autoLangDetect_switch)
        functionCallingSwitch = findViewById(R.id.function_calling_switch)
        desktopMode = findViewById(R.id.desktop_switch)
        imagineSwitch = findViewById(R.id.imagine_switch)
        btnClearChat = findViewById(R.id.btn_clear_chat)
        btnDebugMenu = findViewById(R.id.btn_debug_menu)
        btnModel = findViewById(R.id.btn_model)
        btnPrompt = findViewById(R.id.btn_prompt)
        btnSystem = findViewById(R.id.btn_system)
        promptDesc = findViewById(R.id.prompt_desc)
        modelDesc = findViewById(R.id.model_desc)
        btnAbout = findViewById(R.id.btn_about)
        btnClassicView = findViewById(R.id.btn_classic_chat)
        btnBubblesView = findViewById(R.id.btn_bubbles_chat)
        dalleResolutions = findViewById(R.id.model_for)
        btnDalle2 = findViewById(R.id.dalle2)
        btnDalle3 = findViewById(R.id.dalle3)
        r256 = findViewById(R.id.r256)
        r512 = findViewById(R.id.r512)
        r1024 = findViewById(R.id.r1024)
        audioGoogle = findViewById(R.id.google)
        audioWhisper = findViewById(R.id.whisper)
        gpt30 = findViewById(R.id.gpt30)
        gpt40 = findViewById(R.id.gpt40)
        assistantLanguage = findViewById(R.id.btn_manage_language)
        btnModelGroup = findViewById(R.id.btn_model_s_for)
        btnAutoLanguageDetect = findViewById(R.id.btn_auto_lang_detect)
        btnVoiceSelector = findViewById(R.id.btn_manage_voices)
        btnGoogleTTS = findViewById(R.id.tts_google)
        btnOpenAITTS = findViewById(R.id.tts_openai)
        btnExperiments = findViewById(R.id.btnExperiments)
        btnBack = findViewById(R.id.btn_back)
        ad = findViewById(R.id.ad)

        btnChangeApi?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_top_item_background)!!, this)

        btnChangeAccount?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnCustomHost?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnSetAssistant?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        assistantLanguage?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnVoiceSelector?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnModel?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnPrompt?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnSystem?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background)!!, this)

        btnBubblesView?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v3)!!, this)

        btnClassicView?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v3)!!, this)

        findViewById<LinearLayout>(R.id.btn_dalle_version)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_dalle_resolution)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_audio_source)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_tts_source)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_model_s)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_layout)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_silence_mode)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_always_speak)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_function_calling)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_imagine)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        findViewById<LinearLayout>(R.id.btn_desktop)!!.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        btnAutoLanguageDetect?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_center_item_background_noclick)!!, this)

        btnAbout?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.t_menu_bottom_item_background)!!, this)

        btnExperiments?.background = getDarkAccentDrawableV2(btnExperiments?.background!!)
        btnBack?.background = getDarkAccentDrawableV2(btnBack?.background!!)

        initChatId()

        btnBack?.setOnClickListener {
            finish()
        }

        /* EXPERIMENTAL PART */

        btnExperiments?.setOnClickListener {
            startActivity(Intent(this, SettingsV2Activity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", chatId))
            finish()
        }
    }

    private fun initSettings() {
        preferences = Preferences.getPreferences(this, chatId)

        if (preferences?.getExperimentalUI() == true) {
            startActivity(Intent(this, SettingsV2Activity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", chatId))
            finish()
        }

        if (preferences?.getAdsEnabled()!!) {
            MobileAds.initialize(this) { /* unused */ }

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(TestDevicesAds.TEST_DEVICES)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)

            val adView = AdView(this)
            adView.setAdSize(AdSize.LARGE_BANNER)
            adView.adUnitId =
                if (preferences?.getDebugTestAds()!!) getString(R.string.ad_banner_unit_id_test) else getString(
                    R.string.ad_banner_unit_id
                )

            ad?.addView(adView)

            val adRequest: AdRequest = AdRequest.Builder().build()

            adView.loadAd(adRequest)

            adView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad?.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    ad?.visibility = View.VISIBLE
                }
            }
        } else {
            ad?.visibility = View.GONE
        }

        activationPrompt = preferences?.getPrompt().toString()
        systemMessage = preferences?.getSystemMessage().toString()

        if (preferences?.getLayout() == "bubbles") {
            switchUIToBubbles()
        } else {
            switchUIToClassic()
        }

        silenceSwitch?.isChecked = preferences?.getSilence() == true
        alwaysSpeak?.isChecked = preferences?.getNotSilence() == true

        desktopMode?.isChecked = preferences?.getDesktopMode() == true

        functionCallingSwitch?.isChecked = preferences?.getFunctionCalling() == true
        imagineSwitch?.isChecked = preferences?.getImagineCommand() == true

        btnGoogleTTS?.isChecked = preferences?.getTtsEngine() == "google"
        btnOpenAITTS?.isChecked = preferences?.getTtsEngine() == "openai"

        if (preferences?.getSilence() == true) {
            alwaysSpeak?.isEnabled = false
        }

        if (preferences?.getNotSilence() == true) {
            silenceSwitch?.isEnabled = false
        }

        autoLangDetectSwitch?.isChecked = preferences?.getAutoLangDetect() == true

        ttsEngine = preferences?.getTtsEngine().toString()

        loadResolution()
        loadModel()
        loadLanguage()

        initLogic()
    }

    private fun initLogic() {
        btnClassicView?.setOnClickListener {
            preferences?.setLayout("classic")
            switchUIToClassic()
        }

        btnBubblesView?.setOnClickListener {
            preferences?.setLayout("bubbles")
            switchUIToBubbles()
        }

        if (preferences?.getDalleVersion() == "2") {
            btnDalle2?.isChecked = true
        } else {
            btnDalle3?.isChecked = true
        }

        btnChangeApi?.setOnClickListener {
            val apiKeyDialog: ApiKeyDialog = ApiKeyDialog.newInstance("")
            apiKeyDialog.setStateChangedListener(apiChangedListener)
            apiKeyDialog.show(supportFragmentManager.beginTransaction(), "ApiKeyDialog")
        }

        btnChangeAccount?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://platform.openai.com/account")
            startActivity(intent)
        }

        btnSetAssistant?.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        btnAbout?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java).putExtra("chatId", chatId))
        }

        btnClearChat?.setOnClickListener {
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

        btnModel?.setOnClickListener {
            val advancedSettingsDialogFragment: AdvancedSettingsDialogFragment = AdvancedSettingsDialogFragment.newInstance(model, chatId)
            advancedSettingsDialogFragment.setStateChangedListener(modelChangedListener)
            advancedSettingsDialogFragment.show(supportFragmentManager.beginTransaction(), "AdvancedSettingsDialog")
        }

        assistantLanguage?.setOnClickListener {
            val languageSelectorDialogFragment: LanguageSelectorDialogFragment = LanguageSelectorDialogFragment.newInstance(language, chatId)
            languageSelectorDialogFragment.setStateChangedListener(languageChangedListener)
            languageSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "LanguageSelectorDialog")
        }

        btnPrompt?.setOnClickListener {
            val promptDialog: ActivationPromptDialogFragment = ActivationPromptDialogFragment.newInstance(activationPrompt)
            promptDialog.setStateChangedListener(promptChangedListener)
            promptDialog.show(supportFragmentManager.beginTransaction(), "PromptDialog")
        }

        btnSystem?.setOnClickListener {
            val promptDialog: SystemMessageDialogFragment = SystemMessageDialogFragment.newInstance(systemMessage)
            promptDialog.setStateChangedListener(systemChangedListener)
            promptDialog.show(supportFragmentManager.beginTransaction(), "SystemMessageDialog")
        }

        btnCustomHost?.setOnClickListener {
            val hostnameEditorDialog: HostnameEditorDialog = HostnameEditorDialog.newInstance(preferences?.getCustomHost()!!)
            hostnameEditorDialog.setStateChangedListener(hostChangedListener)
            hostnameEditorDialog.show(supportFragmentManager.beginTransaction(), "HostEditorDialog")
        }

        btnVoiceSelector?.setOnClickListener {
            val voiceSelectorDialogFragment: VoiceSelectorDialogFragment = VoiceSelectorDialogFragment.newInstance(if (preferences?.getTtsEngine() == "google") preferences?.getVoice() ?: "" else preferences?.getOpenAIVoice() ?: "", chatId, ttsEngine)
            voiceSelectorDialogFragment.show(supportFragmentManager.beginTransaction(), "VoiceSelectorDialogFragment")
        }

        btnDebugMenu?.setOnClickListener { startActivity(Intent(this, DebugActivity::class.java).putExtra("chatId", chatId)) }

        silenceSwitch?.setOnCheckedChangeListener { _, isChecked -> run {
            preferences?.setSilence(isChecked)

            if (isChecked) {
                preferences?.setNotSilence(false)
                alwaysSpeak?.isChecked = false
                alwaysSpeak?.isEnabled = false
            } else {
                alwaysSpeak?.isEnabled = true
            }
        } }

        alwaysSpeak?.setOnCheckedChangeListener { _, isChecked -> run {
            preferences?.setNotSilence(isChecked)

            if (isChecked) {
                preferences?.setSilence(false)
                silenceSwitch?.isChecked = false
                silenceSwitch?.isEnabled = false
            } else {
                silenceSwitch?.isEnabled = true
            }
        } }

        autoLangDetectSwitch?.setOnCheckedChangeListener { _, isChecked -> preferences?.setAutoLangDetect(isChecked) }

        functionCallingSwitch?.setOnCheckedChangeListener { _, isChecked -> preferences?.setFunctionCalling(isChecked) }

        desktopMode?.setOnCheckedChangeListener { _, isChecked -> preferences?.setDesktopMode(isChecked) }

        imagineSwitch?.setOnCheckedChangeListener { _, isChecked -> preferences?.setImagineCommand(isChecked) }

        r256?.setOnClickListener { saveResolution("256x256") }
        r512?.setOnClickListener { saveResolution("512x512") }
        r1024?.setOnClickListener { saveResolution("1024x1024") }

        audioGoogle?.setOnClickListener { preferences?.setAudioModel("google") }
        audioWhisper?.setOnClickListener { preferences?.setAudioModel("whisper") }

        btnDalle2?.setOnClickListener {
            preferences?.setDalleVersion("2")
        }

        btnDalle3?.setOnClickListener {
            preferences?.setDalleVersion("3")
        }

        btnGoogleTTS?.setOnClickListener {
            preferences?.setTtsEngine("google")
        }

        btnOpenAITTS?.setOnClickListener {
            preferences?.setTtsEngine("openai")
        }

        gpt30?.setOnClickListener {
            model = "gpt-3.5-turbo"
            preferences?.setModel(model)
        }
        gpt40?.setOnClickListener {
            model = "gpt-4"
            preferences?.setModel(model)
        }

        if (preferences?.getAudioModel().toString() == "google") audioGoogle?.isChecked = true
        else audioWhisper?.isChecked = true
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2())
        return drawable
    }

    private fun getDarkAccentDrawableV3(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV3(this))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_5.getColor(context)
    }

    private fun getSurfaceColorV2() : Int {
        return SurfaceColors.SURFACE_5.getColor(this)
    }

    private fun getSurfaceColorV3(context: Context) : Int {
        return ResourcesCompat.getColor(context.resources, R.color.accent_250, context.theme)
    }

    private fun switchUIToClassic() {
        btnBubblesView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v3)
        btnClassicView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v2)

        btnBubblesView?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v3)!!, this)

        btnClassicView?.background = getDarkAccentDrawableV3(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v2)!!)
    }

    private fun switchUIToBubbles() {
        btnBubblesView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v2)
        btnClassicView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v3)

        btnBubblesView?.background = getDarkAccentDrawableV3(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v2)!!)

        btnClassicView?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(this, R.drawable.btn_accent_tonal_selector_v3)!!, this)
    }

    private fun initChatId() {
        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            if (chatId == "") {
                btnClearChat?.isEnabled = false
                activitySettingsTitle?.text = getString(R.string.global_settings_title)
                globalSettingsTip?.visibility = View.VISIBLE
            } else {
                activitySettingsTitle?.text = getString(R.string.chat_settings_title)
                globalSettingsTip?.visibility = View.GONE
            }
        } else {
            btnClearChat?.isEnabled = false
            activitySettingsTitle?.text = getString(R.string.global_settings_title)
            globalSettingsTip?.visibility = View.VISIBLE
        }

        initSettings()
    }

    private fun loadModel() {
        model = preferences?.getModel().toString()

        gpt30?.isChecked = model == "gpt-3.5-turbo"
        gpt40?.isChecked = model == "gpt-4"
    }

    private fun loadResolution() {
        when (preferences?.getResolution()) {
            "256x256" -> r256?.isChecked = true
            "512x512" -> r512?.isChecked = true
            "1024x1024" -> r1024?.isChecked = true
            else -> r512?.isChecked = true
        }
    }

    private fun loadLanguage() {
        language = preferences?.getLanguage().toString()
    }

    private fun saveResolution(resolution: String) {
        preferences?.setResolution(resolution)
    }

    override fun onResume() {
        super.onResume()

        // Reset preferences singleton
        Preferences.getPreferences(this, chatId)
    }
}
