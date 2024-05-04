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

package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.StaticAvatarParser
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class AssistantAdapter(data: ArrayList<HashMap<String, Any>>?, context: FragmentActivity?, override val preferences: Preferences) : AbstractChatAdapter(data, context, preferences) {
    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        if (mContext == null) return convertView!!

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val mView: View? = if (dataArray?.get(position)?.get("isBot") == true) {
            inflater.inflate(R.layout.view_assistant_bot_message, null)
        } else {
            inflater.inflate(R.layout.view_assistant_user_message, null)
        }

        val bubbleBg: ConstraintLayout = mView!!.findViewById(R.id.bubble_bg)

        icon = mView.findViewById(R.id.icon)
        message = mView.findViewById(R.id.message)
        dalleImage = mView.findViewById(R.id.dalle_image)
        btnCopy = mView.findViewById(R.id.btn_copy)

        btnEdit = mView.findViewById(R.id.btn_edit)

        message?.setTextIsSelectable(true)

        if (dataArray?.get(position)?.get("isBot") == true) {
            if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                bubbleBg.setBackgroundResource(R.drawable.bubble_out_dark)
                message?.setTextColor(ResourcesCompat.getColor(mContext.resources, R.color.white, null))
            }
        } else {
            if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                bubbleBg.setBackgroundResource(R.drawable.bubble_in_dark)
                message?.setTextColor(ResourcesCompat.getColor(mContext.resources, R.color.white, null))
            }
        }

        super.getView(position, mView, parent)

        if (preferences.getAvatarType() == "builtin") {
            icon?.setImageResource(StaticAvatarParser.parse(preferences.getAvatarId()))
            DrawableCompat.setTint(icon?.getDrawable()!!, ContextCompat.getColor(mContext, R.color.accent_900))
        } else {
            readAndDisplay(Uri.fromFile(File(mContext.getExternalFilesDir("images")?.absolutePath + "/avatar_" + preferences.getAvatarId() + ".png")))
        }

        return mView
    }

    private fun readAndDisplay(uri: Uri) {
        val bitmap = readFile(uri)

        if (bitmap != null) {
            icon?.setImageBitmap(roundCorners(bitmap, 80f))
        }
    }

    private fun readFile(uri: Uri) : Bitmap? {
        return mContext?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { _ ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun roundCorners(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        // Create a bitmap with the same size as the original.
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        // Prepare a canvas with the new bitmap.
        val canvas = Canvas(output)

        // The paint used to draw the original bitmap onto the new one.
        val paint = Paint().apply {
            isAntiAlias = true
            color = -0xbdbdbe
        }

        // The rectangle bounds for the original bitmap.
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        // Draw rounded rectangle as background.
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}
