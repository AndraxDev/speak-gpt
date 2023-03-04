package org.teslasoft.assistant.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.materialswitch.MaterialSwitch
import org.teslasoft.assistant.R
import org.teslasoft.assistant.onboarding.ActivationActivity

class SettingsActivity : FragmentActivity() {

    private var btnChangeApi: LinearLayout? = null
    private var btnChangeAccount: LinearLayout? = null
    private var silenceSwitch: MaterialSwitch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnChangeApi = findViewById(R.id.btn_manage_api)
        btnChangeAccount = findViewById(R.id.btn_manage_account)
        silenceSwitch = findViewById(R.id.silent_switch)

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
}