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
                "development" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_development, mContext.requireActivity().theme)
                "music" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_music, mContext.requireActivity().theme)
                "art" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_art, mContext.requireActivity().theme)
                "culture" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_culture, mContext.requireActivity().theme)
                "business" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_business, mContext.requireActivity().theme)
                "gaming" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_gaming, mContext.requireActivity().theme)
                "education" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_education, mContext.requireActivity().theme)
                "history" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_history, mContext.requireActivity().theme)
                "health" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_health, mContext.requireActivity().theme)
                "food" ->ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_food, mContext.requireActivity().theme)
                "tourism" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_tourism, mContext.requireActivity().theme)
                "productivity" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_productivity, mContext.requireActivity().theme)
                "tools" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_tools, mContext.requireActivity().theme)
                "entertainment" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_entertainment, mContext.requireActivity().theme)
                "sport" -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_cat_sport, mContext.requireActivity().theme)
                else -> ResourcesCompat.getColor(mContext.requireActivity().resources, R.color.bg_grey, mContext.requireActivity().theme)
            }

            when (item["category"]) {
                "development" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_development), mContext)
                "music" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_music), mContext)
                "art" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_art), mContext)
                "culture" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_culture), mContext)
                "business" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_business), mContext)
                "gaming" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_gaming), mContext)
                "education" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_education), mContext)
                "history" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_history), mContext)
                "health" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_health), mContext)
                "food" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_food), mContext)
                "tourism" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tourism), mContext)
                "productivity" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_productivity), mContext)
                "tools" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_tools), mContext)
                "entertainment" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_entertainment), mContext)
                "sport" -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.cat_sport), mContext)
                else -> applyColorToCard(bgColor, ContextCompat.getColor(mContext.requireActivity(), R.color.grey), mContext)
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
