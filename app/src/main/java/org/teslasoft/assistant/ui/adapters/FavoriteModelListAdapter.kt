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

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

/** ListView adapter to display list of voices */
class FavoriteModelListAdapter(private val context: Context, private val items: ArrayList<Map<String, String>>, private var chatId: String, private var apiEndpointId: String) : BaseAdapter() {

    private var listener: OnItemClickListener? = null

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.view_model, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = getItem(position) as Map<String, String>
        viewHolder.textView.text = item.get("modelId")

        val preferences: Preferences = Preferences.getPreferences(context, chatId)

        if (preferences.getModel() == item.get("modelId")) {
            viewHolder.voiceBg.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(context, R.drawable.btn_accent_tonal_selector_v4)!!, context)

            viewHolder.textView.setTextColor(ContextCompat.getColor(context, R.color.accent_250))

            viewHolder.modelAction.setImageResource(R.drawable.ic_close_item_inv)
        } else {
            viewHolder.voiceBg.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(context, R.drawable.btn_accent_tonal_selector_v3)!!, context)

            viewHolder.textView.setTextColor(ContextCompat.getColor(context, R.color.text))

            viewHolder.modelAction.setImageResource(R.drawable.ic_close_item)
        }

        viewHolder.modelAction.tooltipText = context.getString(R.string.label_remove_from_favorites)
        viewHolder.modelAction.contentDescription = context.getString(R.string.label_remove_from_favorites)

        viewHolder.voiceBg.setOnClickListener {
            listener?.onItemClick(item.get("modelId")!!, item.get("endpointId")!!)
        }

        viewHolder.modelAction.setOnClickListener {
            listener?.onActionClick(item.get("modelId")!!, item.get("endpointId")!!, position)
        }

        return view
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
        return context.getColor(android.R.color.transparent)
    }

    private fun getSurfaceColorV2(context: Context) : Int {
        return context.getColor(R.color.accent_900)
    }

    private class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.voice_name)
        val voiceBg: ConstraintLayout = view.findViewById(R.id.voice_bg)
        val modelAction: ImageButton = view.findViewById(R.id.btn_action)
    }

    interface OnItemClickListener {
        fun onItemClick(model: String, endpointId: String)
        fun onActionClick(model: String, endpointId: String, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
