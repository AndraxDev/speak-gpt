package org.teslasoft.assistant.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.teslasoft.assistant.ImageBrowserActivity
import org.teslasoft.assistant.R

class ChatAdapter(data: ArrayList<HashMap<String, Any>>?, context: Activity) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, Any>>? = data
    private val mContext: Activity = context

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
        val dalleImage: ImageView = mView.findViewById(R.id.dalle_image)
        val imageFrame: LinearLayout = mView.findViewById(R.id.image_frame)

        message.setTextIsSelectable(true)

        if (dataArray?.get(position)?.get("isBot") == true) {
            icon.setImageResource(R.drawable.assistant)
            username.text = mContext.resources.getString(R.string.app_name)
            ui.setBackgroundResource(R.color.accent_100)
        }
        else {
            icon.setImageResource(R.drawable.ic_user)
            username.text = "User"
            ui.setBackgroundResource(R.color.window_background)
        }

        if (dataArray?.get(position)?.get("message").toString().contains("data:image/png")) {
            imageFrame.visibility = View.VISIBLE
            message.visibility = View.GONE

            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(convertDpToPixel(24f, mContext).toInt()))
            Glide.with(mContext).load(Uri.parse(dataArray?.get(position)?.get("message").toString())).apply(requestOptions).into(dalleImage)

            dalleImage.setOnClickListener {
                val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("tmp", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("tmp", dataArray?.get(position)?.get("message").toString())
                editor.apply()

                val intent = Intent(mContext, ImageBrowserActivity::class.java)
                intent.putExtra("tmp", "1")
                mContext.startActivity(intent)
            }
        } else {
            message.text = dataArray?.get(position)?.get("message").toString()
            imageFrame.visibility = View.GONE
            message.visibility = View.VISIBLE
        }

        return mView
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}