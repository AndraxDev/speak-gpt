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

package org.teslasoft.assistant.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.ActivationPromptDialog
import org.teslasoft.assistant.ui.fragments.ModelDialogFragment
import org.teslasoft.assistant.ui.onboarding.ActivationActivity

class SettingsActivity : FragmentActivity() {

    private var btnChangeApi: LinearLayout? = null
    private var btnChangeAccount: LinearLayout? = null
    private var btnSetAssistant: LinearLayout? = null
    private var silenceSwitch: MaterialSwitch? = null
    private var btnClearChat: MaterialButton? = null
    private var btnDebugMenu: MaterialButton? = null
    private var dalleResolutions: MaterialButtonToggleGroup? = null
    private var btnModel: LinearLayout? = null
    private var btnPrompt: LinearLayout? = null
    private var btnAbout: LinearLayout? = null
    private var r256: MaterialButton? = null
    private var r512: MaterialButton? = null
    private var r1024: MaterialButton? = null
    private var audioGoogle: MaterialButton? = null
    private var audioWhisper: MaterialButton? = null
    private var promptDesc: TextView? = null
    private var modelDesc: TextView? = null
    private var btnClassicView: LinearLayout? = null
    private var btnBubblesView: LinearLayout? = null

    private var preferences: Preferences? = null
    private var chatId = ""
    private var model = ""
    private var activationPrompt : String = ""

    private var modelChangedListener: ModelDialogFragment.StateChangesListener = object : ModelDialogFragment.StateChangesListener {
        override fun onSelected(name: String, maxTokens: String, endSeparator: String) {
            model = name
            preferences?.setModel(name)
            preferences?.setMaxTokens(maxTokens.toInt())
            preferences?.setEndSeparator(endSeparator)
            modelDesc?.text = model
        }

        override fun onFormError(name: String, maxTokens: String, endSeparator: String) {
            if (name == "") Toast.makeText(this@SettingsActivity, "Error, no model name is provided", Toast.LENGTH_SHORT).show()
            else if (name.contains("gpt-4")) Toast.makeText(this@SettingsActivity, "Error, GPT4 support maximum of 8192 tokens", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this@SettingsActivity, "Error, more than 2048 tokens is not supported", Toast.LENGTH_SHORT).show()
            val modelDialogFragment: ModelDialogFragment = ModelDialogFragment.newInstance(model)
            modelDialogFragment.setStateChangedListener(this)
            modelDialogFragment.show(supportFragmentManager.beginTransaction(), "ModelDialog")
        }
    }

    private var promptChangedListener: ActivationPromptDialog.StateChangesListener = object : ActivationPromptDialog.StateChangesListener {
        override fun onEdit(prompt: String) {
            activationPrompt = prompt

            preferences?.setPrompt(prompt)

            if (activationPrompt != "") {
                promptDesc?.text = activationPrompt
            } else {
                promptDesc?.text = resources.getString(R.string.activation_prompt_set_message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUI()
        initSettings()
        initChatId()
        initLogic()
    }

    private fun initUI() {
        setContentView(R.layout.activity_settings)
        btnChangeApi = findViewById(R.id.btn_manage_api)
        btnChangeAccount = findViewById(R.id.btn_manage_account)
        btnSetAssistant = findViewById(R.id.btn_manage_assistant)
        silenceSwitch = findViewById(R.id.silent_switch)
        btnClearChat = findViewById(R.id.btn_clear_chat)
        btnDebugMenu = findViewById(R.id.btn_debug_menu)
        btnModel = findViewById(R.id.btn_model)
        btnPrompt = findViewById(R.id.btn_prompt)
        promptDesc = findViewById(R.id.prompt_desc)
        modelDesc = findViewById(R.id.model_desc)
        btnAbout = findViewById(R.id.btn_about)
        btnClassicView = findViewById(R.id.btn_classic_chat)
        btnBubblesView = findViewById(R.id.btn_bubbles_chat)
        dalleResolutions = findViewById(R.id.model_for)
        r256 = findViewById(R.id.r256)
        r512 = findViewById(R.id.r512)
        r1024 = findViewById(R.id.r1024)
        audioGoogle = findViewById(R.id.google)
        audioWhisper = findViewById(R.id.whisper)
    }

    private fun initSettings() {
        preferences = Preferences.getPreferences(this)

        activationPrompt = preferences?.getPrompt().toString() // possible kotlin bug

        if (preferences?.getLayout() == "bubbles") {
            switchUIToBubbles()
        } else {
            switchUIToClassic()
        }

        promptDesc?.text = if (activationPrompt != "") {
            activationPrompt
        } else {
            resources.getString(R.string.activation_prompt_set_message)
        }

        silenceSwitch?.isChecked = preferences?.getSilence() == true

        loadResolution()
        loadModel()
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

        btnChangeApi?.setOnClickListener {
            startActivity(Intent(this, ActivationActivity::class.java))
            finish()
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
            startActivity(Intent(this, AboutActivity::class.java))
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
            val modelDialogFragment: ModelDialogFragment = ModelDialogFragment.newInstance(model)
            modelDialogFragment.setStateChangedListener(modelChangedListener)
            modelDialogFragment.show(supportFragmentManager.beginTransaction(), "ModelDialog")
        }

        btnPrompt?.setOnClickListener {
            val promptDialog: ActivationPromptDialog = ActivationPromptDialog.newInstance(activationPrompt)
            promptDialog.setStateChangedListener(promptChangedListener)
            promptDialog.show(supportFragmentManager.beginTransaction(), "PromptDialog")
        }

        btnDebugMenu?.setOnClickListener { startActivity(Intent(this, DebugActivity::class.java)) }

        silenceSwitch?.setOnCheckedChangeListener { _, isChecked -> preferences?.setSilence(isChecked) }

        r256?.setOnClickListener { saveResolution("256x256") }
        r512?.setOnClickListener { saveResolution("512x512") }
        r1024?.setOnClickListener { saveResolution("1024x1024") }

        audioGoogle?.setOnClickListener { preferences?.setAudioModel("google") }
        audioWhisper?.setOnClickListener { preferences?.setAudioModel("whisper") }

        if (preferences?.getAudioModel().toString() == "google") audioGoogle?.isChecked = true
        else audioWhisper?.isChecked = true
    }

    private fun switchUIToClassic() {
        btnBubblesView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v3)
        btnClassicView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v2)
    }

    private fun switchUIToBubbles() {
        btnBubblesView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v2)
        btnClassicView?.setBackgroundResource(R.drawable.btn_accent_tonal_selector_v3)
    }

    private fun initChatId() {
        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            if (chatId == "") {
                btnClearChat?.visibility = View.GONE
            }
        } else {
            btnClearChat?.visibility = View.GONE
        }
    }

    private fun loadModel() {
        model = preferences?.getModel().toString() // Possible kotlin bug
        modelDesc?.text = model
    }

    private fun loadResolution() {
        when (preferences?.getResolution()) {
            "256x256" -> r256?.isChecked = true
            "512x512" -> r512?.isChecked = true
            "1024x1024" -> r1024?.isChecked = true
            else -> r512?.isChecked = true
        }
    }

    private fun saveResolution(resolution: String) {
        preferences?.setResolution(resolution)
    }
}