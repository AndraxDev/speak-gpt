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

import org.teslasoft.assistant.ui.activities.ChatActivity
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
        val textFirstMessage: TextView = mView.findViewById(R.id.chat_first_message)
        val modelName: TextView = mView.findViewById(R.id.model_name)

        icon.setImageResource(R.drawable.chatgpt_icon)

        name.text = if (dataArray?.get(position)?.get("name").toString().trim().contains("_autoname_")) "Untitled chat" else dataArray?.get(position)?.get("name").toString()

        textFirstMessage.text = dataArray?.get(position)?.get("first_message") ?: "No messages yet."

        val model: String = Preferences.getPreferences(mContext.requireActivity(), Hash.hash(dataArray?.get(position)?.get("name").toString())).getModel()

        val textModel: TextView = mView.findViewById(R.id.textModel)

        modelName.text = model

        textModel.text = when (model) {
            "gpt-4" -> "GPT 4"
            "gpt-4-1106-preview" -> "GPT 4 Turbo"
            "gpt-4-turbo-2024-04-09" -> "GPT 4 Turbo"
            "gpt-4-0125-preview" -> "GPT 4 Turbo"
            "gpt-4-turbo-preview" -> "GPT 4 Turbo"
            "gpt-4-32k" -> "GPT 4"
            "gpt-3.5-turbo" -> "GPT 3.5"
            "gpt-3.5-turbo-1106" -> "GPT 3.5"
            "gpt-3.5-turbo-0125" -> "GPT 3.5 (0125)"
            "gemma-7b-it" -> "GROQ"
            "llama2-70b-4096" -> "GROQ"
            "mixtral-8x7b-32768" -> "GROQ"
            else -> "FT"
        }

        selector.setBackgroundResource(R.drawable.btn_accent_tonal_selector_tint)

        selector.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity())
        icon.setBackgroundResource(R.drawable.btn_accent_tonal_v3)

        icon.background = getDarkAccentDrawableV2(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_v3)!!, mContext.requireActivity())

        when (textModel.text) {
            "GPT 4" -> {
                updateCard(selector, icon, R.color.tint_red, R.color.gpt_icon_red)
            }
            "GPT 3.5" -> {
                updateCard(selector, icon, R.color.tint_yellow, R.color.gpt_icon_yellow)
            }
            "GPT 3.5 (0125)" -> {
                updateCard(selector, icon, R.color.tint_purple, R.color.gpt_icon_purple)
            }
            "GPT 4 Turbo" -> {
                updateCard(selector, icon, R.color.tint_green, R.color.gpt_icon_green)
            }
            "FT" -> {
                updateCard(selector, icon, R.color.tint_blue, R.color.gpt_icon_blue)
            }
            "GROQ" -> {
                updateCard(selector, icon, R.color.tint_orange, R.color.gpt_icon_orange)
            }
            else -> {
                icon.setImageResource(R.drawable.chatgpt_icon)
                DrawableCompat.setTint(icon.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), R.color.accent_900))
            }
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
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance(dataArray?.get(position)?.get("name").toString(), false, false, false)
            chatDialogFragment.setStateChangedListener((mContext as ChatsListFragment).chatListUpdatedListener)
            chatDialogFragment.show(mContext.parentFragmentManager.beginTransaction(), "AddChatDialog")

            return@setOnLongClickListener true
        }

        return mView
    }

    private fun updateCard(selector: ConstraintLayout, icon: ImageView, tintColor: Int, iconColor: Int) {
        selector.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity().getColor(tintColor))

        icon.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_transparent)!!, mContext.requireActivity().getColor(tintColor))

        icon.setImageResource(R.drawable.chatgpt_icon)
        DrawableCompat.setTint(icon.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), iconColor))
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getAccentDrawable(drawable: Drawable, color: Int) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), color)
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
