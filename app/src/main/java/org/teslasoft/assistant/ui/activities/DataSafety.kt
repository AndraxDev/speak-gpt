package org.teslasoft.assistant.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.R

class DataSafety : FragmentActivity() {

    private var btnDecline: MaterialButton? = null
    private var btnAccept: MaterialButton? = null
    private var btnPrivacyPolicy: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_consent)

        btnDecline = findViewById(R.id.btn_decline)
        btnAccept = findViewById(R.id.btn_accept)
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy)

        btnDecline?.setOnClickListener {
            val sharedPref: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean("consent", false)
            editor.apply()
            finish()
        }

        btnAccept?.setOnClickListener {
            val sharedPref: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean("consent", true)
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnPrivacyPolicy?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = android.net.Uri.parse("https://teslasoft.org/privacy")
            startActivity(intent)
        }
    }
}