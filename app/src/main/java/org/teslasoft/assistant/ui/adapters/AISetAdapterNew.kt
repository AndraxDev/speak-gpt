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

package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.GlobalPreferences

class AISetAdapterNew(private val mContext: Context, private val dataArray: ArrayList<Map<String, String>>, private val listener: OnInteractionListener) : RecyclerView.Adapter<AISetAdapterNew.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.view_ai_set, parent, false)
        return ViewHolder(view, mContext, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val map: Map<String, String> = dataArray[position]
        holder.bind(map)
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    class ViewHolder(itemView: View, private val mContext: Context, private val listener: OnInteractionListener) : RecyclerView.ViewHolder(itemView) {

        private var window: MaterialCardView = itemView.findViewById(R.id.window)
        private var setIcon: ImageView = itemView.findViewById(R.id.set_icon)
        private var setIconBg: ImageView = itemView.findViewById(R.id.set_icon_bg)
        private var setName: TextView = itemView.findViewById(R.id.set_name)
        private var setDescription: TextView = itemView.findViewById(R.id.set_description)
        private var setOwner: TextView = itemView.findViewById(R.id.set_owner)
        private var setModel: TextView = itemView.findViewById(R.id.set_model)
        private var btnUseGlobally: MaterialButton = itemView.findViewById(R.id.btn_use_globally)
        private var btnCreateChat: MaterialButton = itemView.findViewById(R.id.btn_create_chat)
        private var btnGetApiKey: MaterialButton = itemView.findViewById(R.id.btn_get_api_key)

        @SuppressLint("SetTextI18n")
        fun bind(data: Map<String, String>) {
            if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(mContext).getAmoledPitchBlack()) {
                window.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_50))
                btnUseGlobally.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_200))
                btnGetApiKey.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_200))
                setIconBg.imageTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_200))
            } else {
                setIconBg.imageTintList = ColorStateList.valueOf(SurfaceColors.SURFACE_4.getColor(mContext))
            }

            setName.text = data["name"]
            setDescription.text = data["desc"]
            setOwner.text = mContext.getString(R.string.label_provided_by) + " " + data["owner"]
            setModel.text = "AI Model: ${data["model"]}"

            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners((28f * mContext.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT).toInt()))
            Glide.with(mContext)
                .load("https://" + Config.API_SERVER_NAME + "/api/v1/exp/" + data["icon"])
                .apply(requestOptions)
                .into(setIcon)

            btnCreateChat.setOnClickListener {
                listener.onCreateChatClick(data["model"] ?: "", data["apiEndpoint"] ?: "", data["apiEndpointName"] ?: "", data["suggestedChatName"] ?: "", data["avatarType"] ?: "", data["avatarId"] ?: "", data["assistantName"] ?: "")
            }

            btnUseGlobally.setOnClickListener {
                listener.onUseGloballyClick(data["model"] ?: "", data["apiEndpoint"] ?: "", data["apiEndpointName"] ?: "", data["avatarType"] ?: "", data["avatarId"] ?: "", data["assistantName"] ?: "")
            }

            btnGetApiKey.setOnClickListener {
                listener.onGetApiKeyClicked(data["apiKeyUrl"] ?: "")
            }

            val animation: Animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in)
            animation.duration = 300
            animation.startOffset = 50
            itemView.startAnimation(animation)
        }

        private fun isDarkThemeEnabled(): Boolean {
            return when (mContext.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                else -> false
            }
        }
    }

    interface OnInteractionListener {
        fun onUseGloballyClick(model: String, endpointUrl: String, endpointName: String, avatarType: String, avatarId: String, assistantName: String)
        fun onCreateChatClick(model: String, endpointUrl: String, endpointName: String, suggestedChatName: String, avatarType: String, avatarId: String, assistantName: String)
        fun onGetApiKeyClicked(apiKeyUrl: String)
    }
}
