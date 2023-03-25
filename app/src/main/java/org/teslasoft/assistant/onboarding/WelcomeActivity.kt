package org.teslasoft.assistant.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.R

class WelcomeActivity : FragmentActivity() {

    private var btnNext: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btnNext = findViewById(R.id.btn_next)

        btnNext?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
            finish()
        }
    }
}