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

package org.teslasoft.assistant.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Base64

class ShareUtil {
    companion object {
        fun shareBase64Image(context: Context, base64Image: String, imageType: String) {
            try {
                // Step 1: Decode Base64 to Bitmap
                val decodedString = Base64.getDecoder().decode(base64Image.replace( if (imageType == "png") "data:image/png;base64," else "data:image/jpeg;base64,", ""))
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                // Step 2: Save Bitmap to a file
                val cachePath = File(context.externalCacheDir, "cached_images")
                cachePath.mkdirs() // Make sure the directories exist
                val fileName = "shared_image.$imageType"
                val file = File(cachePath, fileName)
                val fileOutputStream: OutputStream = FileOutputStream(file)
                if (imageType == "png") {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                }
                fileOutputStream.flush()
                fileOutputStream.close()

                // Step 3: Get URI from the file
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                // Step 4: Share intent
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = if (imageType == "png") "image/png" else "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            } catch (e: Exception) {
                (context as Activity).runOnUiThread {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Debug: Error")
                        .setMessage("Failed to share image: ${e.stackTraceToString()}")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                e.printStackTrace()
            }
        }

        fun sharePlainText(context: Context, text: String) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Message"))
        }
    }
}