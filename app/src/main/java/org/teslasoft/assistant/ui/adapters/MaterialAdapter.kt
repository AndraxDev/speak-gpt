package org.teslasoft.assistant.ui.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.teslasoft.assistant.R

class MaterialAdapter(private val items: List<Int>) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.material_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background = itemView.findViewById<LinearLayout>(R.id.carousel_item_background)

        fun bind(item: Int) {
            val colorDrawable = ColorDrawable(item)
            colorDrawable.alpha = 150
            background.background = colorDrawable
        }
    }
}