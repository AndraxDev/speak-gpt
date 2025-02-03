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

package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
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

@Deprecated("Use AISetAdapterNew instead")
class AISetAdapter(private val mContext: Context, private val dataArray: ArrayList<Map<String, String>>) : BaseAdapter() {

    private var window: MaterialCardView? = null
    private var setIcon: ImageView? = null
    private var setIconBg: ImageView? = null
    private var setName: TextView? = null
    private var setDescription: TextView? = null
    private var setOwner: TextView? = null
    private var setModel: TextView? = null
    private var btnUseGlobally: MaterialButton? = null
    private var btnCreateChat: MaterialButton? = null
    private var btnGetApiKey: MaterialButton? = null

    private var listener: OnInteractionListener? = null

    fun setOnInteractionListener(listener: OnInteractionListener) {
        this.listener = listener
    }

    override fun getCount(): Int {
        return dataArray.size
    }

    override fun getItem(position: Int): Any {
        return dataArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_ai_set, parent, false)
        }

        window = mView?.findViewById(R.id.window)
        setIcon = mView?.findViewById(R.id.set_icon)
        setIconBg = mView?.findViewById(R.id.set_icon_bg)
        setName = mView?.findViewById(R.id.set_name)
        setDescription = mView?.findViewById(R.id.set_description)
        setOwner = mView?.findViewById(R.id.set_owner)
        setModel = mView?.findViewById(R.id.set_model)
        btnUseGlobally = mView?.findViewById(R.id.btn_use_globally)
        btnCreateChat = mView?.findViewById(R.id.btn_create_chat)
        btnGetApiKey = mView?.findViewById(R.id.btn_get_api_key)

        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(mContext).getAmoledPitchBlack()) {
            window?.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_50))
            btnUseGlobally?.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_200))
            btnGetApiKey?.backgroundTintList = ColorStateList.valueOf(mContext.getColor(R.color.amoled_accent_200))

            val drawable = ResourcesCompat.getDrawable(mContext.resources, R.drawable.avd_static, null)
            val drawable2 = getAccentAmoledDrawable(drawable!!)

            Glide.with(mContext).load(drawable2).into(setIconBg!!)
        } else {
            val drawable = ResourcesCompat.getDrawable(mContext.resources, R.drawable.avd_static, null)
            Glide.with(mContext).load(drawable!!).into(setIconBg!!)
        }

        setName?.text = dataArray[position]["name"]
        setDescription?.text = dataArray[position]["desc"]
        setOwner?.text = mContext.getString(R.string.label_provided_by) + " " + dataArray[position]["owner"]
        setModel?.text = "AI Model: ${dataArray[position]["model"]}"

        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(dpToPx(28f).toInt()))
        Glide.with(mContext)
            .load("https://" + Config.API_SERVER_NAME + "/api/v1/exp/" + dataArray[position]["icon"])
            .apply(requestOptions)
            .into(setIcon!!)

        btnCreateChat?.setOnClickListener {
            listener?.onCreateChatClick(dataArray[position]["model"] ?: "", dataArray[position]["apiEndpoint"] ?: "", dataArray[position]["apiEndpointName"] ?: "", dataArray[position]["suggestedChatName"] ?: "", dataArray[position]["avatarType"] ?: "", dataArray[position]["avatarId"] ?: "", dataArray[position]["assistantName"] ?: "")
        }

        btnUseGlobally?.setOnClickListener {
            listener?.onUseGloballyClick(dataArray[position]["model"] ?: "", dataArray[position]["apiEndpoint"] ?: "", dataArray[position]["apiEndpointName"] ?: "", dataArray[position]["avatarType"] ?: "", dataArray[position]["avatarId"] ?: "", dataArray[position]["assistantName"] ?: "")
        }

        btnGetApiKey?.setOnClickListener {
            listener?.onGetApiKeyClicked(dataArray[position]["apiKeyUrl"] ?: "")
        }

        val animation: Animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in)
        animation.duration = 300
        animation.startOffset = 50
        mView?.startAnimation(animation)

        return mView!!
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

    interface OnInteractionListener {
        fun onUseGloballyClick(model: String, endpointUrl: String, endpointName: String, avatarType: String, avatarId: String, assistantName: String)
        fun onCreateChatClick(model: String, endpointUrl: String, endpointName: String, suggestedChatName: String, avatarType: String, avatarId: String, assistantName: String)
        fun onGetApiKeyClicked(apiKeyUrl: String)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * mContext.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    }

    private fun getSurfaceColor() : Int {
        return SurfaceColors.SURFACE_5.getColor(mContext)
    }

    private fun getAccentDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor())
        return drawable
    }

    private fun getAccentAmoledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), mContext.getColor(R.color.amoled_accent_200))
        return drawable
    }
}