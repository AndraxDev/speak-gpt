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

package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment

import com.google.android.material.elevation.SurfaceColors

import org.teslasoft.assistant.ui.ChatActivity
import org.teslasoft.assistant.ui.fragments.tabs.ChatsListFragment
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.dialogs.AddChatDialogFragment
import org.teslasoft.assistant.util.Hash

class ChatListAdapter(data: ArrayList<HashMap<String, String>>?, context: Fragment) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, String>>? = data
    private val mContext: Fragment = context

    override fun getCount(): Int {
        return if (dataArray == null) 0 else dataArray.size
    }

    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_chat_name, null)
        }

        val name: TextView = mView!!.findViewById(R.id.name)
        val selector: ConstraintLayout = mView.findViewById(R.id.chat_selector)
        val icon: ImageView = mView.findViewById(R.id.chat_icon)
        val modelName: TextView = mView.findViewById(R.id.model_name)

        icon.setImageResource(R.drawable.chatgpt_icon)

        name.text = dataArray?.get(position)?.get("name").toString()

        val model: String = Preferences.getPreferences(mContext.requireActivity(), Hash.hash(dataArray?.get(position)?.get("name").toString())).getModel()

        val textModel: TextView = mView.findViewById(R.id.textModel)

        modelName.text = model

        textModel.text = when (model) {
            "gpt-4" -> "GPT 4"
            "gpt-4-1106-preview" -> "GPT 4 Turbo"
            "gpt-4-0314" -> "GPT 4"
            "gpt-4-0613" -> "GPT 4"
            "gpt-4-32k" -> "GPT 4"
            "gpt-4-32k-0314" -> "GPT 4"
            "gpt-3.5-turbo" -> "GPT 3.5"
            "gpt-3.5-turbo-0613" -> "GPT 3.5"
            "gpt-3.5-turbo-0301" -> "GPT 3.5"
            "text-davinci-003" -> "DAVINCI"
            "text-davinci-002" -> "DAVINCI"
            "text-curie-001" -> "CURIE"
            "text-babbage-001" -> "BABBAGE"
            "text-ada-001" -> "ADA"
            "davinci" -> "DAVINCI"
            "curie" -> "CURIE"
            "babbage" -> "BABBAGE"
            "ada" -> "ADA"
            else -> "FT"
        }

        if (position % 2 == 0) {
            selector.setBackgroundResource(R.drawable.btn_accent_selector_v2)
            selector.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_selector_v2)!!, mContext.requireActivity())
            icon.setBackgroundResource(R.drawable.btn_accent_tonal_v3)

            icon.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_v3)!!, mContext.requireActivity())
        } else {
            selector.setBackgroundResource(R.drawable.btn_accent_selector)
            icon.setBackgroundResource(R.drawable.btn_accent_tonal_v3)

            icon.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_v3)!!, mContext.requireActivity())
        }

        selector.setOnClickListener {
            val i = Intent(
                    mContext.requireActivity(),
                    ChatActivity::class.java
            ).setAction(Intent.ACTION_VIEW)

            i.putExtra("name", dataArray?.get(position)?.get("name").toString())
            i.putExtra("chatId", Hash.hash(dataArray?.get(position)?.get("name").toString()))

            mContext.requireActivity().startActivity(i)
        }

        selector.setOnLongClickListener {
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance(name.text.toString(), false)
            chatDialogFragment.setStateChangedListener((mContext as ChatsListFragment).chatListUpdatedListener)
            chatDialogFragment.show(mContext.parentFragmentManager.beginTransaction(), "AddChatDialog")

            return@setOnLongClickListener true
        }

        return mView
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_2.getColor(context)
    }

    private fun getSurfaceColorV2(context: Context) : Int {
        return SurfaceColors.SURFACE_5.getColor(context)
    }
}
