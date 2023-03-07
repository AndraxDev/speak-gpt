package org.teslasoft.assistant

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.client.OpenAI
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.onboarding.WelcomeActivity

class DebugActivity : FragmentActivity() {

    private var key: String? = null

    private var ai: OpenAI? = null

    private var btnDebug: MaterialButton? = null

    private var textDebug: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(this, "Nothing interesting here, finishing... :)", Toast.LENGTH_SHORT).show()

        finish()

        /*setContentView(R.layout.activity_debug)

        btnDebug = findViewById(R.id.btnDebug)
        textDebug = findViewById(R.id.debugText)

        textDebug?.setTextIsSelectable(true)

        initAI()

        btnDebug?.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                testImages()
            }
        }*/
    }

    private fun initAI() {
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        key = settings.getString("api_key", null)

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            ai = OpenAI(key!!)
        }
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun testImages() {
        val images = ai?.imageURL( // or openAI.imageJSON
            creation = ImageCreation(
                prompt = "A cute baby sea otter",
                n = 2,
                size = ImageSize.is1024x1024
            )
        )

        textDebug?.text = images?.get(0)?.url
    }
}