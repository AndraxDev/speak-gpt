package org.teslasoft.assistant.settings

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import org.teslasoft.assistant.DebugActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.onboarding.ActivationActivity

class SettingsActivity : FragmentActivity() {

    private var btnChangeApi: LinearLayout? = null
    private var btnChangeAccount: LinearLayout? = null
    private var silenceSwitch: MaterialSwitch? = null
    private var btnClearChat: MaterialButton? = null
    private var btnDebugMenu: MaterialButton? = null
    private var dalleResolutions: MaterialButtonToggleGroup? = null
    private var r256: MaterialButton? = null
    private var r512: MaterialButton? = null
    private var r1024: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnChangeApi = findViewById(R.id.btn_manage_api)
        btnChangeAccount = findViewById(R.id.btn_manage_account)
        silenceSwitch = findViewById(R.id.silent_switch)
        btnClearChat = findViewById(R.id.btn_clear_chat)
        btnDebugMenu = findViewById(R.id.btn_debug_menu)

        dalleResolutions = findViewById(R.id.resolution_choices)
        r256 = findViewById(R.id.r256)
        r512 = findViewById(R.id.r512)
        r1024 = findViewById(R.id.r1024)

        loadResolution()

        r256?.setOnClickListener { saveResolution("256x256") }
        r512?.setOnClickListener { saveResolution("512x512") }
        r1024?.setOnClickListener { saveResolution("1024x1024") }

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

        btnClearChat?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Confirm")
                .setMessage("Are you sure? This action can not be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    run {
                        val sharedPreferences: SharedPreferences = getSharedPreferences("chat", MODE_PRIVATE)
                        val editor: Editor = sharedPreferences.edit()
                        editor.putString("chat", "[]")
                        editor.apply()
                        Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }

        btnDebugMenu?.setOnClickListener {
            startActivity(Intent(this, DebugActivity::class.java))
        }

        val silenceSettings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val silenceMode = silenceSettings.getBoolean("silence_mode", false)

        silenceSwitch?.isChecked = silenceMode

        silenceSwitch?.setOnCheckedChangeListener { _, isChecked ->
            run {
                val editor: SharedPreferences.Editor = silenceSettings.edit()
                if (isChecked) {
                    editor.putBoolean("silence_mode", true)
                } else {
                    editor.putBoolean("silence_mode", false)
                }

                editor.apply()
            }
        }
    }

    private fun loadResolution() {
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val r = settings.getString("resolution", "512x512")

        when (r) {
            "256x256" -> r256?.isChecked = true
            "512x512" -> r512?.isChecked = true
            "1024x1024" -> r1024?.isChecked = true
            else -> r512?.isChecked = true
        }
    }

    private fun saveResolution(r: String) {
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = settings.edit()
        editor.putString("resolution", r)
        editor.apply()
    }
}