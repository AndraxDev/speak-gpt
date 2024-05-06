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
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import org.teslasoft.assistant.util.StaticAvatarParser
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ChatListAdapter(data: ArrayList<HashMap<String, String>>?, context: Fragment) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, String>>? = data
    private val mContext: Fragment = context
    private var preferences: Preferences? = null

    override fun getCount(): Int {
        return if (dataArray == null) 0 else dataArray.size
    }

    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private var icon: ImageView? = null

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_chat_name, null)
        }

        val name: TextView = mView!!.findViewById(R.id.name)
        val selector: ConstraintLayout = mView.findViewById(R.id.chat_selector)
        icon = mView.findViewById(R.id.chat_icon)
        val textFirstMessage: TextView = mView.findViewById(R.id.chat_first_message)
        val modelName: TextView = mView.findViewById(R.id.model_name)

        preferences = Preferences.getPreferences(mContext.requireActivity(), "")

        if (preferences?.getAvatarTypeByChatId(Hash.hash(dataArray?.get(position)?.get("name").toString()), mContext.requireActivity()) == "builtin") {
            icon?.setImageResource(StaticAvatarParser.parse(preferences?.getAvatarIdByChatId(Hash.hash(dataArray?.get(position)?.get("name").toString()), mContext.requireActivity())!!))
        } else {
            readAndDisplay(Uri.fromFile(File(mContext.requireActivity().getExternalFilesDir("images")?.absolutePath + "/avatar_" + preferences?.getAvatarIdByChatId(Hash.hash(dataArray?.get(position)?.get("name").toString()), mContext.requireActivity()) + ".png")))
        }

        name.text = if (dataArray?.get(position)?.get("name").toString().trim().contains("_autoname_")) mContext.getString(R.string.label_untitled_chat) else dataArray?.get(position)?.get("name").toString()

        textFirstMessage.text = dataArray?.get(position)?.get("first_message") ?: "No messages yet."

        val model: String = Preferences.getPreferences(mContext.requireActivity(), Hash.hash(dataArray?.get(position)?.get("name").toString())).getModel()

        val textModel: TextView = mView.findViewById(R.id.textModel)

        icon?.contentDescription = name.text

        modelName.text = model

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

        selector.setBackgroundResource(R.drawable.btn_accent_tonal_selector_tint)

        selector.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity())
        icon!!.setBackgroundResource(R.drawable.btn_accent_tonal_v3)

        icon!!.background = getDarkAccentDrawableV2(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_v3)!!, mContext.requireActivity())

        when (textModel.text) {
            "GPT 4", "GEMINI" -> {
                updateCard(selector, icon!!, R.color.tint_red, R.color.gpt_icon_red, position)
            }
            "GPT 3.5 Turbo", "GEMMA" -> {
                updateCard(selector, icon!!, R.color.tint_yellow, R.color.gpt_icon_yellow, position)
            }
            "GPT 3.5 (0125)", "PERPLEXITY" -> {
                updateCard(selector, icon!!, R.color.tint_purple, R.color.gpt_icon_purple, position)
            }
            "GPT 4 Turbo", "CLAUDE" -> {
                updateCard(selector, icon!!, R.color.tint_green, R.color.gpt_icon_green, position)
            }
            "MISTRAL", "META" -> {
                updateCard(selector, icon!!, R.color.tint_orange, R.color.gpt_icon_orange, position)
            }
            "CUSTOM" -> {
                updateCard(selector, icon!!, R.color.tint_blue, R.color.gpt_icon_blue, position)
            }
            else -> {
                icon!!.setImageResource(R.drawable.chatgpt_icon)
                DrawableCompat.setTint(icon!!.getDrawable(), ContextCompat.getColor(mContext.requireActivity(), R.color.accent_900))
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
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance(true, dataArray?.get(position)?.get("name").toString(), false, false, false, "", "", "", "", "")
            chatDialogFragment.setStateChangedListener((mContext as ChatsListFragment).chatListUpdatedListener)
            chatDialogFragment.show(mContext.parentFragmentManager.beginTransaction(), "AddChatDialog")

            return@setOnLongClickListener true
        }

        val animation: Animation = AnimationUtils.loadAnimation(mContext.context, R.anim.fade_in)
        animation.duration = 200
        mView.startAnimation(animation)

        return mView
    }

    private fun updateCard(selector: ConstraintLayout, icon: ImageView, tintColor: Int, iconColor: Int, position: Int) {
        selector.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_selector_tint)!!, mContext.requireActivity().getColor(tintColor))

        icon.background = getAccentDrawable(
            ContextCompat.getDrawable(mContext.requireActivity(), R.drawable.btn_accent_tonal_transparent)!!, mContext.requireActivity().getColor(tintColor))

        if (preferences?.getAvatarTypeByChatId(Hash.hash(dataArray?.get(position)?.get("name").toString()), mContext.requireActivity()) == "builtin") {
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

    private fun readAndDisplay(uri: Uri) {
        val bitmap = readFile(uri)

        if (bitmap != null) {
            icon?.setImageBitmap(roundCorners(bitmap, 80f))
        }
    }

    private fun readFile(uri: Uri) : Bitmap? {
        return mContext.requireActivity().contentResolver?.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { _ ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun roundCorners(bitmap: Bitmap, cornerRadius: Float): Bitmap {
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
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}
