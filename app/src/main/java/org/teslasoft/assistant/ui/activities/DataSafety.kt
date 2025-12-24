/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import androidx.core.content.edit
import androidx.core.net.toUri

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
            sharedPref.edit {
                putBoolean("consent", false)
            }
            finish()
        }

        btnAccept?.setOnClickListener {
            val sharedPref: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)
            sharedPref.edit {
                putBoolean("consent", true)
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnPrivacyPolicy?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = "https://${Config.API_SERVER_NAME}/privacy".toUri()
            startActivity(intent)
        }
    }
}
