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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

import androidx.fragment.app.FragmentActivity

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class ChatAdapter(data: ArrayList<HashMap<String, Any>>?, context: FragmentActivity, override val preferences: Preferences) : AbstractChatAdapter(data, context, preferences) {

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val layout  = preferences.getLayout()

        val mView: View? = if (layout == "bubbles") {
            if (dataArray?.get(position)?.get("isBot") == true) {
                inflater.inflate(R.layout.view_assistant_bot_message, null)
            } else {
                inflater.inflate(R.layout.view_assistant_user_message, null)
            }
        } else {
            inflater.inflate(R.layout.view_message, null)
        }

        val bubbleBg: ConstraintLayout? = mView?.findViewById(R.id.bubble_bg)

        ui = mView!!.findViewById(R.id.ui)
        icon = mView.findViewById(R.id.icon)
        message = mView.findViewById(R.id.message)
        val username: TextView = mView.findViewById(R.id.username)
        dalleImage = mView.findViewById(R.id.dalle_image)
        btnCopy = mView.findViewById(R.id.btn_copy)

        btnCopy?.background = getSurface3Drawable(AppCompatResources.getDrawable(mContext, R.drawable.btn_accent_tonal)!!, mContext)

        super.getView(position, mView, parent)

        if (layout == "bubbles") {
            if (dataArray?.get(position)?.get("isBot") == true) {
                icon?.setImageResource(R.drawable.assistant)

                if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                    bubbleBg?.setBackgroundResource(R.drawable.bubble_out_dark)
                    message?.setTextColor(ResourcesCompat.getColor(mContext.resources, R.color.white, null))
                }
            } else {
                icon?.setImageResource(R.drawable.ic_user)

                if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                    bubbleBg?.setBackgroundResource(R.drawable.bubble_in_dark)
                    message?.setTextColor(ResourcesCompat.getColor(mContext.resources, R.color.white, null))
                }
            }
        } else {
            if (dataArray?.get(position)?.get("isBot") == true) {
                icon?.setImageResource(R.drawable.assistant)
                username.text = mContext.resources.getString(R.string.app_name)
                ui?.setBackgroundColor(getSurfaceColor(mContext))
            } else {
                icon?.setImageResource(R.drawable.ic_user)
                username.text = "User"
                btnCopy?.visibility = View.VISIBLE
                ui?.setBackgroundColor(getSurface2Color(mContext))
            }
        }

        return mView
    }
}
