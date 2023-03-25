package org.teslasoft.assistant

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PromptViewActivity : FragmentActivity() {

    private var activityTitle: TextView? = null

    private var id = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.accent_100)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            finish()
        } else {
            id = extras.getString("id", "")
            title = extras.getString("title", "")

            if (id == "" || title == "") {
                finish()
            } else {
                setContentView(R.layout.activity_view_prompt)
                activityTitle = findViewById(R.id.activity_view_title)

                activityTitle?.text = title
            }
        }
    }
}