package org.teslasoft.assistant.settings

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.teslasoft.assistant.R

class SettingsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}