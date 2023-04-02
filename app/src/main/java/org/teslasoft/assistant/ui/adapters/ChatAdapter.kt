package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import org.teslasoft.assistant.R

class ChatAdapter(data: ArrayList<HashMap<String, Any>>?, context: FragmentActivity) : AbstractChatAdapter(data, context) {

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val settings: SharedPreferences = mContext.getSharedPreferences("settings",
            FragmentActivity.MODE_PRIVATE
        )

        val layout  = settings.getString("layout", "classic").toString()

        val mView: View? = if (layout == "bubbles") {
            if (dataArray?.get(position)?.get("isBot") == true) {
                inflater.inflate(R.layout.view_assistant_bot_message, null)
            } else {
                inflater.inflate(R.layout.view_assistant_user_message, null)
            }
        } else {
            inflater.inflate(R.layout.view_message, null)
        }

        ui = mView!!.findViewById(R.id.ui)
        icon = mView.findViewById(R.id.icon)
        message = mView.findViewById(R.id.message)
        val username: TextView = mView.findViewById(R.id.username)
        dalleImage = mView.findViewById(R.id.dalle_image)
        imageFrame = mView.findViewById(R.id.image_frame)
        btnCopy = mView.findViewById(R.id.btn_copy)

        super.getView(position, mView, parent)

        if (layout == "bubbles") {
            if (dataArray?.get(position)?.get("isBot") == true) {
                icon?.setImageResource(R.drawable.assistant)
            } else {
                icon?.setImageResource(R.drawable.ic_user)
            }
        } else {
            if (dataArray?.get(position)?.get("isBot") == true) {
                icon?.setImageResource(R.drawable.assistant)
                username.text = mContext.resources.getString(R.string.app_name)
                ui?.setBackgroundColor(getSurfaceColor(mContext))
            } else {
                icon?.setImageResource(R.drawable.ic_user)
                username.text = "User"
                ui?.setBackgroundResource(R.color.window_background)
                btnCopy?.visibility = View.GONE
            }
        }

        return mView
    }
}