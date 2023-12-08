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
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment

import org.teslasoft.assistant.ui.activities.PromptViewActivity
import org.teslasoft.assistant.R

class PromptAdapter(data: ArrayList<HashMap<String, String>>?, context: Fragment) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, String>>? = data
    private val mContext: Fragment = context

    override fun getCount(): Int {
        return dataArray!!.size
    }

    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private var background: LinearLayout? = null
    private var promptName: TextView? = null
    private var promptDescription: TextView? = null
    private var promptAuthor: TextView? = null
    private var likesCounter: TextView? = null
    private var textFor: TextView? = null
    private var likeIcon: LinearLayout? = null

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_prompt, null)
        }

        background = mView!!.findViewById(R.id.bg)
        promptName = mView.findViewById(R.id.prompt_name)
        promptDescription = mView.findViewById(R.id.prompt_description)
        promptAuthor = mView.findViewById(R.id.prompt_author)
        likesCounter = mView.findViewById(R.id.likes_count)
        textFor = mView.findViewById(R.id.text_for)
        likeIcon = mView.findViewById(R.id.like_icon)

        when (dataArray?.get(position)?.get("category")) {
            "development" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_development), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_development))
            "music" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_music), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_music))
            "art" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_art), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_art))
            "culture" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_culture), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_culture))
            "business" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_business), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_business))
            "gaming" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_gaming), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_gaming))
            "education" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_education), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_education))
            "history" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_history), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_history))
            "health" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_health), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_health))
            "food" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_food), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_food))
            "tourism" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_tourism), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tourism))
            "productivity" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_productivity), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_productivity))
            "tools" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_tools), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tools))
            "entertainment" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_entertainment), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_entertainment))
            "sport" -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_cat_sport), ContextCompat.getColor(mContext.requireActivity(), R.color.cat_sport))
            else -> applyColorToCard(ContextCompat.getColor(mContext.requireActivity(), R.color.tint_grey), ContextCompat.getColor(mContext.requireActivity(), R.color.grey))
        }

        promptName?.text = dataArray?.get(position)?.get("name")
        promptDescription?.text = dataArray?.get(position)?.get("desc")
        promptAuthor?.text = "By " + dataArray?.get(position)?.get("author")
        likesCounter?.text = dataArray?.get(position)?.get("likes")
        textFor?.text = dataArray?.get(position)?.get("type")

        background?.setOnClickListener {
            val i = Intent(mContext.requireActivity(), PromptViewActivity::class.java).setAction(Intent.ACTION_VIEW)
            i.putExtra("id", dataArray?.get(position)?.get("id"))
            i.putExtra("title", dataArray?.get(position)?.get("name"))
            mContext.requireActivity().startActivity(i)
        }

        return mView
    }

    private fun applyColorToCard(tintColor: Int, color: Int) {
        promptName?.setTextColor(color)
        textFor?.setTextColor(color)
        likesCounter?.setTextColor(color)
        background?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(),
                R.drawable.btn_accent_tonal_selector)!!, tintColor)

        likeIcon?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(),
                R.drawable.ic_like)!!, color)
    }

    private fun getDarkAccentDrawable(drawable: Drawable, color: Int) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), color)
        return drawable
    }
}
