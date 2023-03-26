package org.teslasoft.assistant

import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.FragmentActivity
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.fragments.AssistantFragment

class AssistantActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val assistantFragment = AssistantFragment()
        assistantFragment.show(supportFragmentManager, "AssistantFragment")

        window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(this)

        assistantFragment.dialog?.setOnDismissListener {
            finish()
        }
    }
}