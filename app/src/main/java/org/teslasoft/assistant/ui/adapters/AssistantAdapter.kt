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
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

import androidx.fragment.app.FragmentActivity

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

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

        // btnCopy?.background = getSurface3Drawable(AppCompatResources.getDrawable(mContext, R.drawable.btn_accent_tonal)!!, mContext)

        super.getView(position, mView, parent)

        icon?.setImageResource(R.drawable.assistant)

        return mView
    }
}
