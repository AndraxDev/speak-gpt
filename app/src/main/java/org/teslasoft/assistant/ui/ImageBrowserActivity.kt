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

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.R
import uk.co.senab.photoview.PhotoViewAttacher

class ImageBrowserActivity : FragmentActivity() {

    private var image: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_imageview)

        window.navigationBarColor = 0xFF000000.toInt()
        window.statusBarColor = 0xFF000000.toInt()

        image = findViewById(R.id.image)

        val attacher = PhotoViewAttacher(image)
        attacher.update()

        val b: Bundle? = intent.extras

        if (b != null) {
            CoroutineScope(Dispatchers.Main).launch {
                load()
            }
        } else {
            finish()
        }
    }

    private fun load() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("tmp", MODE_PRIVATE)
        val url: String? = sharedPreferences.getString("tmp", null)

        if (url != null) {
            Glide.with(this).load(Uri.parse(url)).into(image!!)
        } else {
            finish()
        }
    }
}