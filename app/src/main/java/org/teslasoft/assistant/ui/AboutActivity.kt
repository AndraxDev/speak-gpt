/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.R


class AboutActivity : FragmentActivity() {

    private var appIcon: ImageView? = null
    private var btnProjects: MaterialButton? = null
    private var btnTerms: MaterialButton? = null
    private var btnPrivacy: MaterialButton? = null
    private var btnFeedback: MaterialButton? = null
    private var appVer: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        appIcon = findViewById(R.id.app_icon)
        btnProjects = findViewById(R.id.btn_projects)
        btnTerms = findViewById(R.id.btn_terms)
        btnPrivacy = findViewById(R.id.btn_privacy)
        btnFeedback = findViewById(R.id.btn_feedback)
        appVer = findViewById(R.id.app_ver)

        appIcon?.setImageResource(R.drawable.assistant)

        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName

            appVer?.text = "App version: $version"
        } catch (e: PackageManager.NameNotFoundException) {
            appVer?.text = "App version: unknown"
        }

        btnProjects?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://andrax.teslasoft.org/")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnTerms?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://teslasoft.org/tos")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnPrivacy?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://teslasoft.org/privacy")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnFeedback?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("mailto:dostapenko82@gmail.com")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }
    }
}