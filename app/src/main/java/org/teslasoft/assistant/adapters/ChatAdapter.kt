package org.teslasoft.assistant.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.teslasoft.assistant.R

class ChatAdapter(data: ArrayList<Map<String, Any>>?, context: Context) : BaseAdapter() {
    private val dataArray: ArrayList<Map<String, Any>>? = data
    private val mContext: Context = context

    override fun getCount(): Int {
        return dataArray!!.size
    }

    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_message, null)
        }

        val ui: ConstraintLayout = mView!!.findViewById(R.id.ui)
        val icon: ImageView = mView.findViewById(R.id.icon)
        val message: TextView = mView.findViewById(R.id.message)
        val username: TextView = mView.findViewById(R.id.username)

        message.setTextIsSelectable(true)

        if (dataArray?.get(position)?.get("isBot") == true) {
            icon.setImageResource(R.drawable.chatgpt_icon)
            username.text = "Bot"
            ui.setBackgroundResource(R.color.accent_100)
        }
        else {
            icon.setImageResource(R.drawable.ic_user)
            username.text = "User"
            ui.setBackgroundResource(R.color.window_background)
        }

        message.text = dataArray?.get(position)?.get("message").toString()

        return mView
    }
}