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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.R
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64

class ImageBrowserActivity : FragmentActivity() {

    private var image: ImageView? = null
    private var btnDownload: FloatingActionButton? = null
    private var fileContents: ByteArray? = null
    private var attacher: PhotoViewAttacher? = null
    private var layoutLoading: LinearLayout? = null
    private var height: Int = 0
    private var width: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_imageview)

        if (android.os.Build.VERSION.SDK_INT <= 34) {
            window.navigationBarColor = 0xFF000000.toInt()
            window.statusBarColor = 0xFF000000.toInt()
        }

        image = findViewById(R.id.image)
        btnDownload = findViewById(R.id.btn_download)
        layoutLoading = findViewById(R.id.layout_loading)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        attacher = PhotoViewAttacher(image!!)
        attacher?.scaleType = ImageView.ScaleType.FIT_CENTER
        attacher?.scale = width / height.toFloat()
        attacher?.maximumScale = 10.0f
        attacher?.update()

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

            Handler(Looper.getMainLooper()).postDelayed({
                attacher?.update()
                val fadeOut: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                layoutLoading?.startAnimation(fadeOut)

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                    override fun onAnimationEnd(animation: Animation) {
                        layoutLoading?.visibility = LinearLayout.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
                })
            }, 500)

            btnDownload?.setOnClickListener {
                val imageType = url.substringAfter("data:image/").substringBefore(";")

                val fileEncoded = url.replace("data:image/$imageType;base64,", "")
                fileContents = Base64.getDecoder().decode(fileEncoded)

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/$imageType"
                    putExtra(Intent.EXTRA_TITLE, if (imageType == "jpeg") "image.jpg" else "image.png")
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/storage/emulated/0/Pictures/SpeakGPT/exported.${if (imageType == "jpeg") "jpg" else "png"}"))
                }
                fileSaveIntentLauncher.launch(intent)
            }
        } else {
            finish()
        }
    }

    private val fileSaveIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    writeToFile(uri)
                }
            }
        }
    }

    private fun writeToFile(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        fileContents
                    )
                }
            }
            Toast.makeText(this, getString(R.string.message_saved), Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, getString(R.string.message_save_failed), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(this, getString(R.string.message_save_failed), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
