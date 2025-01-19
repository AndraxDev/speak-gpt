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
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.activities.PromptViewActivity

class PromptAdapterNew(private val data: ArrayList<HashMap<String, String>>?, private val mContext: Fragment) : RecyclerView.Adapter<PromptAdapterNew.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var background: MaterialCardView = view.findViewById(R.id.tile_bg)
        private var promptName: TextView = view.findViewById(R.id.prompt_name)
        private var promptDescription: TextView = view.findViewById(R.id.prompt_description)
        private var promptAuthor: TextView = view.findViewById(R.id.prompt_author)
        private var likesCounter: TextView = view.findViewById(R.id.likes_count)
        private var textFor: TextView = view.findViewById(R.id.text_for)
        private var likeIcon: LinearLayout = view.findViewById(R.id.like_icon)

        @SuppressLint("SetTextI18n")
        fun bind(item: HashMap<String, String>, mContext: Fragment) {
            val bgColor = when (item["category"]) {
                "development" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_development, mContext.requireActivity().theme), mContext)
                "music" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_music, mContext.requireActivity().theme), mContext)
                "art" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_art, mContext.requireActivity().theme), mContext)
                "culture" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_culture, mContext.requireActivity().theme), mContext)
                "business" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_business, mContext.requireActivity().theme), mContext)
                "gaming" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_gaming, mContext.requireActivity().theme), mContext)
                "education" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_education, mContext.requireActivity().theme), mContext)
                "history" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_history, mContext.requireActivity().theme), mContext)
                "health" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_health, mContext.requireActivity().theme), mContext)
                "food" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_food, mContext.requireActivity().theme), mContext)
                "tourism" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_tourism, mContext.requireActivity().theme), mContext)
                "productivity" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_productivity, mContext.requireActivity().theme), mContext)
                "tools" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_tools, mContext.requireActivity().theme), mContext)
                "entertainment" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_entertainment, mContext.requireActivity().theme), mContext)
                "sport" -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_sport, mContext.requireActivity().theme), mContext)
                else -> harmonizeColors(ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_grey, mContext.requireActivity().theme), mContext)
            }

            when (item["category"]) {
                "development" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_development), mContext), mContext)
                "music" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_music), mContext), mContext)
                "art" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_art), mContext), mContext)
                "culture" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_culture), mContext), mContext)
                "business" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_business), mContext), mContext)
                "gaming" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_gaming), mContext), mContext)
                "education" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_education), mContext), mContext)
                "history" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_history), mContext), mContext)
                "health" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_health), mContext), mContext)
                "food" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_food), mContext), mContext)
                "tourism" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tourism), mContext), mContext)
                "productivity" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_productivity), mContext), mContext)
                "tools" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tools), mContext), mContext)
                "entertainment" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_entertainment), mContext), mContext)
                "sport" -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.cat_sport), mContext), mContext)
                else -> applyColorToCard(bgColor, harmonizeColors(ContextCompat.getColor(mContext.requireActivity(), R.color.grey), mContext), mContext)
            }

            promptName.text = item["name"]
            promptDescription.text = item["desc"]
            promptAuthor.text = "By " + item["author"]
            likesCounter.text = item["likes"]
            textFor.text = item["type"]

            background.setOnClickListener {
                val intent = Intent(mContext.requireActivity(), PromptViewActivity::class.java).setAction(Intent.ACTION_VIEW)
                intent.putExtra("id", item["id"])
                intent.putExtra("title", item["name"])
                intent.putExtra("category", item["category"])

                // Creating a pair for shared element transition
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    mContext.requireActivity() as Activity,
                    Pair.create(background, ViewCompat.getTransitionName(background)),
                    Pair.create(promptName, ViewCompat.getTransitionName(promptName)),
                )

                mContext.requireActivity().startActivity(intent, options.toBundle())
            }

            val animation: Animation = AnimationUtils.loadAnimation(mContext.context, R.anim.fade_in)
            animation.duration = 200
            animation.startOffset = 30
            view.startAnimation(animation)
        }

        private fun harmonizeColors(color: Int, mContext: Fragment) : Int {
            return MaterialColors.harmonize(color, ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.accent_600, mContext.requireActivity().theme))
        }

        private fun applyColorToCard(tintColor: Int, color: Int, mContext: Fragment) {
            promptName.setTextColor(color)
            textFor.setTextColor(color)
            likesCounter.setTextColor(color)
            background.backgroundTintList = ColorStateList.valueOf(tintColor)

            likeIcon.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(mContext.requireActivity(),
                    R.drawable.ic_like)!!, color)
        }

        private fun getDarkAccentDrawable(drawable: Drawable, color: Int) : Drawable {
            DrawableCompat.setTint(DrawableCompat.wrap(drawable), color)
            return drawable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_prompt, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data!![position]
        holder.bind(item, mContext)
    }
}
