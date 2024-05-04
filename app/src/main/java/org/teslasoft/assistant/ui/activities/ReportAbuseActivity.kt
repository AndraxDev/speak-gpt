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
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.Config.Companion.API_ENDPOINT
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.core.api.network.RequestNetwork

class ReportAbuseActivity : FragmentActivity() {

    private var id = ""
    private var reason = ""

    private var reportForm: ScrollView? = null
    private var loadingBar: ProgressBar? = null

    private var btnIllegal: RadioButton? = null
    private var btnCp: RadioButton? = null
    private var btnHate: RadioButton? = null
    private var btnMalware: RadioButton? = null
    private var btnFraud: RadioButton? = null
    private var btnAdult: RadioButton? = null
    private var btnPolitical: RadioButton? = null
    private var btnDuplicated: RadioButton? = null
    private var btnCatIncorrect: RadioButton? = null
    private var reportActivityTitle: TextView? = null

    private var fieldDetails: EditText? = null
    private var btnSend: MaterialButton? = null

    private var btnBack: ImageButton? = null

    private var requestNetwork: RequestNetwork? = null
    private val reportListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            reportForm?.visibility = View.VISIBLE
            loadingBar?.visibility = View.GONE

            Toast.makeText(this@ReportAbuseActivity, resources.getString(R.string.prompt_report_success), Toast.LENGTH_SHORT).show()
            finish()
        }

        override fun onErrorResponse(tag: String, message: String) {
            reportForm?.visibility = View.VISIBLE
            loadingBar?.visibility = View.GONE
            Toast.makeText(this@ReportAbuseActivity, resources.getString(R.string.prompt_report_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            finish()
        } else {
            id = extras.getString("id", "")

            if (id == "" || title == "") {
                finish()
            } else {
                initUI()
            }
        }
    }

    private fun initUI() {
        setContentView(R.layout.activity_report_prompt)

        reportForm = findViewById(R.id.report_form)
        loadingBar = findViewById(R.id.loading_bar)
        btnIllegal = findViewById(R.id.btn_illegal)
        btnCp = findViewById(R.id.btn_cp)
        btnHate = findViewById(R.id.btn_hate)
        btnMalware = findViewById(R.id.btn_malware)
        btnFraud = findViewById(R.id.btn_fraud)
        btnAdult = findViewById(R.id.btn_adult)
        btnPolitical = findViewById(R.id.btn_political)
        btnDuplicated = findViewById(R.id.btn_duplicated)
        fieldDetails = findViewById(R.id.field_details)
        btnSend = findViewById(R.id.btn_send_report)
        btnCatIncorrect = findViewById(R.id.btn_cat_incorrect)
        btnBack = findViewById(R.id.btn_back)
        reportActivityTitle = findViewById(R.id.activity_report_title)

        btnBack?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        reportForm?.visibility = View.VISIBLE
        loadingBar?.visibility = View.GONE

        requestNetwork = RequestNetwork(this)

        reloadAmoled()

        initLogic()
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return if (isDarkThemeEnabled() && Preferences.getPreferences(context, "").getAmoledPitchBlack()) {
            ResourcesCompat.getColor(context.resources, R.color.amoled_accent_50, context.theme)
        } else {
            SurfaceColors.SURFACE_4.getColor(context)
        }
    }

    private fun initLogic() {

        btnBack?.setOnClickListener {
            finish()
        }

        btnIllegal?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Illegal activity"
            }
        }

        btnCp?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Child Sexual Abuse"
            }
        }

        btnHate?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Hateful content"
            }
        }

        btnMalware?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Generation of malware"
            }
        }

        btnFraud?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Fraud or deceptive content"
            }
        }

        btnAdult?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Adult content"
            }
        }

        btnPolitical?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Political campaigning or lobbying"
            }
        }

        btnDuplicated?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Duplicated prompt or spam"
            }
        }

        btnCatIncorrect?.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) reason = "Incorrect category"
            }
        }

        btnSend?.setOnClickListener {
            if (reason == "") {
                Toast.makeText(this, "Please select one option", Toast.LENGTH_SHORT).show()
            } else {
                loadData()
            }
        }
    }

    private fun loadData() {
        reportForm?.visibility = View.GONE
        loadingBar?.visibility = View.VISIBLE

        requestNetwork?.startRequestNetwork("GET", "${API_ENDPOINT}/report.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id&reason=$reason&details=${fieldDetails?.text.toString()}", "A", reportListener)
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() &&  Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
            }
            window.setBackgroundDrawableResource(R.color.amoled_window_background)

            reportActivityTitle?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))

            btnBack?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )
        } else {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.window_background, theme)
                window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
            }
            window.setBackgroundDrawableResource(R.color.window_background)

            reportActivityTitle?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))

            btnBack?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )
        }
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }
}
