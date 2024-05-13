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
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.activities.ChatActivity
import org.teslasoft.assistant.util.Hash
import org.teslasoft.assistant.util.StaticAvatarParser
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ChatListAdapter(private val dataArray: ArrayList<HashMap<String, String>>, private val selectorProjection: ArrayList<HashMap<String, String>>, private val mContext: Fragment, private var size: Int = dataArray.size) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    private var preferences: Preferences? = null
    private var bulkActionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_chat_name, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataArray[position], selectorProjection[position], position)
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    fun deleteItemAtPosition(position: Int) {
        dataArray.removeAt(position)
        // selectorProjection.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun editItemAtPosition(position: Int, name: String) {
        dataArray[position]["name"] = name
        dataArray[position]["id"] = Hash.hash(name)
        selectorProjection[position]["name"] = name
        selectorProjection[position]["id"] = Hash.hash(name)
        notifyItemChanged(position)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    fun setBulkActionMode(bulkActionMode: Boolean) {
        this.bulkActionMode = bulkActionMode
    }

    private fun checkSelectionIsEmpty(): Boolean {
        var isEmpty = true

        for (projection in selectorProjection) {
            if (projection["selected"] == "true") {
                isEmpty = false
                break
            }
        }

        return isEmpty
    }

    fun unselectAll() {
        for (projection in selectorProjection) {
            projection["selected"] = "false"
        }

        setBulkActionMode(false)
        listener?.onChangeBulkActionMode(bulkActionMode)
    }

    fun selectAll() {
        for (projection in selectorProjection) {
            projection["selected"] = "true"
        }

        setBulkActionMode(true)
        listener?.onChangeBulkActionMode(bulkActionMode)
    }

    fun edit(position: Int, name: String) {
        listener?.onRename(position, name, Hash.hash(name))
    }

    private var listener: OnInteractionListener? = null

    fun setOnInteractionListener(listener: OnInteractionListener) {
        this.listener = listener
    }

    interface OnInteractionListener {
        fun onRename(position: Int, name: String, id: String)
        fun onBulkSelectionChanged(position: Int, selected: Boolean)
        fun onChangeBulkActionMode(mode: Boolean)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val selector: ConstraintLayout = itemView.findViewById(R.id.chat_selector)
        private val icon: ImageView = itemView.findViewById(R.id.chat_icon)
        private val textFirstMessage: TextView = itemView.findViewById(R.id.chat_first_message)
        private val modelName: TextView = itemView.findViewById(R.id.model_name)
        private val root: ConstraintLayout = itemView.findViewById(R.id.root)
        private val pinMarker: ImageView = itemView.findViewById(R.id.pin_marker)
        private val textModel: TextView = itemView.findViewById(R.id.textModel)

        @SuppressLint("SetTextI18n")
        fun bind(chatMessage: HashMap<String, String>, projection: HashMap<String, String>, position: Int) {
            preferences = Preferences.getPreferences(mContext.requireActivity(), "")

            if (preferences?.getAvatarTypeByChatId(Hash.hash(chatMessage["name"].toString()), mContext.requireActivity()) == "builtin") {
                icon.setImageResource(StaticAvatarParser.parse(preferences?.getAvatarIdByChatId(Hash.hash(chatMessage["name"].toString()), mContext.requireActivity())!!))
            } else {
                readAndDisplay(Uri.fromFile(File(mContext.requireActivity().getExternalFilesDir("images")?.absolutePath + "/avatar_" + preferences?.getAvatarIdByChatId(Hash.hash(chatMessage["name"].toString()), mContext.requireActivity()) + ".png")), icon)
            }

            if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
                root.background = getAccentDrawable(
                    ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_backdrop_amoled)!!, mContext.requireActivity().getColor(R.color.amoled_window_background))
            } else {
                root.background = getAccentDrawable(
                    ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_backdrop)!!, SurfaceColors.SURFACE_0.getColor(mContext.requireActivity()))
            }

            if (chatMessage["pinned"] == "true") {
                pinMarker.visibility = View.VISIBLE
            } else {
                pinMarker.visibility = View.GONE
            }

            name.text = if (chatMessage["name"].toString().trim().contains("_autoname_")) mContext.getString(R.string.label_untitled_chat) else chatMessage["name"].toString()

            textFirstMessage.text = chatMessage["first_message"] ?: "No messages yet."

            val model: String = Preferences.getPreferences(mContext.requireActivity(), Hash.hash(chatMessage["name"].toString())).getModel()

            icon.contentDescription = name.text

            modelName.text = model
            initModelName(model)

            selector.setBackgroundResource(R.drawable.btn_accent_tonal_selector_tint)

            selector.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity())
            icon.setBackgroundResource(R.drawable.btn_accent_tonal_v3)

            icon.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_v3)!!, mContext.requireActivity())

            if (bulkActionMode && (projection["selected"] ?: "false") == "true") {
                updateCard(selector, icon, pinMarker, R.color.accent_300, R.color.accent_900, chatMessage)
            } else {
                reloadCards(chatMessage)
            }

            selector.setOnClickListener {
                if (bulkActionMode) {
                    switchBulkActionState(projection, chatMessage, position)
                } else {
                    val i = Intent(
                        mContext.requireActivity(),
                        ChatActivity::class.java
                    ).setAction(Intent.ACTION_VIEW)

                    i.putExtra("name", chatMessage["name"].toString())
                    i.putExtra("chatId", Hash.hash(chatMessage["name"].toString()))

                    mContext.requireActivity().startActivity(i)
                }
            }

            selector.setOnLongClickListener {
                switchBulkActionState(projection, chatMessage, position)

                return@setOnLongClickListener true
            }

            val animation: Animation = AnimationUtils.loadAnimation(mContext.context, R.anim.fade_in)
            animation.duration = 300
            animation.startOffset = 50
            itemView.startAnimation(animation)
        }

        private fun switchBulkActionState(projection: HashMap<String, String>, chatMessage: HashMap<String, String>, position: Int) {
            if ((projection["selected"] ?: "false") == "true") {
                projection["selected"] = "false"
                reloadCards(chatMessage)
                if (checkSelectionIsEmpty()) setBulkActionMode(false)
            } else {
                setBulkActionMode(true)
                projection["selected"] = "true"
                updateCard(selector, icon, pinMarker, R.color.accent_300, R.color.accent_900, chatMessage)
            }
            listener?.onBulkSelectionChanged(position, (projection["selected"] ?: "false") == "true")
            listener?.onChangeBulkActionMode(bulkActionMode)
        }

        @SuppressLint("SetTextI18n")
        private fun initModelName(model: String) {
            textModel.text = "CUSTOM"
            textModel.text = if (model.lowercase().contains("gemini")) "GEMINI" else textModel.text
            textModel.text = if (model.lowercase().contains("gemma")) "GEMMA" else textModel.text
            textModel.text = if (model.lowercase().contains("mistral") || model.lowercase().contains("mixtral")) "MISTRAL" else textModel.text
            textModel.text = if (model.lowercase().contains("perplexity")) "PERPLEXITY" else textModel.text
            textModel.text = if (model.lowercase().contains("claude")) "CLAUDE" else textModel.text
            textModel.text = if (model.lowercase().contains("llama")) "META" else textModel.text
            textModel.text = if (model.lowercase().contains("gpt-4") && model.lowercase().contains("turbo")) "GPT 4 Turbo" else textModel.text
            textModel.text = if (model.lowercase().contains("gpt-4") && !model.lowercase().contains("turbo")) "GPT 4" else textModel.text
            textModel.text = if (model.lowercase().contains("gpt-3.5") && model.lowercase().contains("turbo") && model.lowercase().contains("0125")) "GPT 3.5 (0125)" else textModel.text
            textModel.text = if (model.lowercase().contains("gpt-3.5") && model.lowercase().contains("turbo") && !model.lowercase().contains("0125")) "GPT 3.5 Turbo" else textModel.text
            textModel.text = if (model.lowercase().contains("gpt-4o")) "GPT 4o" else textModel.text
        }

        private fun reloadCards(chatMessage: HashMap<String, String>) {
            when (textModel.text) {
                "GPT 4", "GEMINI" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_red, R.color.gpt_icon_red, chatMessage)
                }

                "GPT 3.5 Turbo", "GEMMA" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_yellow, R.color.gpt_icon_yellow, chatMessage)
                }

                "GPT 3.5 (0125)", "PERPLEXITY" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_purple, R.color.gpt_icon_purple, chatMessage)
                }

                "GPT 4 Turbo", "CLAUDE" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_green, R.color.gpt_icon_green, chatMessage)
                }

                "MISTRAL", "META" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_orange, R.color.gpt_icon_orange, chatMessage)
                }

                "CUSTOM" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_blue, R.color.gpt_icon_blue, chatMessage)
                }

                "GPT 4o" -> {
                    updateCard(selector, icon, pinMarker, R.color.tint_cyan, R.color.gpt_icon_cyan, chatMessage)
                }

                else -> {
                    icon.setImageResource(R.drawable.chatgpt_icon)
                    DrawableCompat.setTint(icon.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), R.color.accent_900))
                }
            }
        }
    }

    private fun updateCard(selector: ConstraintLayout, icon: ImageView, pin: ImageView, tintColor: Int, iconColor: Int, chatMessage: HashMap<String, String>) {
        selector.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity().getColor(tintColor))

        icon.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_transparent)!!, mContext.requireActivity().getColor(tintColor))

        pin.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_transparent)!!, mContext.requireActivity().getColor(tintColor))

        DrawableCompat.setTint(pin.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), iconColor))

        if (preferences?.getAvatarTypeByChatId(Hash.hash(chatMessage["name"].toString()), mContext.requireActivity()) == "builtin") {
            DrawableCompat.setTint(icon.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), iconColor))
        }
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

    private fun readAndDisplay(uri: Uri, icon: ImageView?) {
        val bitmap = readFile(uri)

        if (bitmap != null) {
            icon?.setImageBitmap(roundCorners(bitmap))
        }
    }

    private fun readFile(uri: Uri) : Bitmap? {
        return mContext.requireActivity().contentResolver?.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { _ ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun roundCorners(bitmap: Bitmap): Bitmap {
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
        canvas.drawRoundRect(rectF, 80.0f, 80.0f, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (mContext.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }
}
