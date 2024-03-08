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

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.R
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

    private var fieldDetails: EditText? = null
    private var btnSend: MaterialButton? = null

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

        window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)

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

        reportForm?.visibility = View.VISIBLE
        loadingBar?.visibility = View.GONE

        requestNetwork = RequestNetwork(this)

        initLogic()
    }

    private fun initLogic() {
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

        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/report.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id&reason=$reason&details=${fieldDetails?.text.toString()}", "A", reportListener)
    }
}
