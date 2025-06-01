/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import eightbitlab.com.blurview.BlurView
import org.teslasoft.assistant.R

class PurposeActivity : FragmentActivity() {
    private var btnNext: MaterialButton? = null
    private var foregroundBlur: BlurView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purpose)

        btnNext = findViewById(R.id.btn_next)
        foregroundBlur = findViewById(R.id.foreground_blur)

        val decorView = window.decorView
        val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
        val windowBackground = decorView.background

        foregroundBlur?.setupWith(rootView)?.setFrameClearDrawable(windowBackground)?.setBlurRadius(250f)

        btnNext?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java).setAction(Intent.ACTION_VIEW))
            finish()
        }
    }
}