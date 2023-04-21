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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.client.OpenAI
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.ui.onboarding.WelcomeActivity
import java.net.URL
import java.util.Base64

class DebugActivity : FragmentActivity() {

    private var key: String? = null

    private var ai: OpenAI? = null

    private var btnDebug: MaterialButton? = null

    private var textDebug: TextView? = null

    private var debugImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(this, "Nothing interesting here, finishing... :)", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun loadImage() {
        val url = URL("https://id.teslasoft.org/smartcard/icon.png")
        val `is` = url.openStream()
        val bytes: ByteArray = org.apache.commons.io.IOUtils.toByteArray(`is`)
        val encoded = Base64.getEncoder().encodeToString(bytes)

        val path = "data:image/png;base64,$encoded"

        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(convertDpToPixel(24f, this).toInt()))
        Glide.with(this).load(Uri.parse(path)).apply(requestOptions).into(debugImage!!)
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
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