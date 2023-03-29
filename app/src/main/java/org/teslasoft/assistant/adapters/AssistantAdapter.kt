package org.teslasoft.assistant.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.elevation.SurfaceColors
import io.noties.markwon.Markwon
import org.teslasoft.assistant.ImageBrowserActivity
import org.teslasoft.assistant.R

class AssistantAdapter(data: ArrayList<HashMap<String, Any>>?, context: FragmentActivity) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, Any>>? = data
    private val mContext: FragmentActivity = context

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

        mView = if (dataArray?.get(position)?.get("isBot") == true) {
            inflater.inflate(R.layout.view_assistant_bot_message, null)
        } else {
            inflater.inflate(R.layout.view_assistant_user_message, null)
        }

        val icon: ImageView = mView.findViewById(R.id.icon)
        val message: TextView = mView.findViewById(R.id.message)
        val dalleImage: ImageView = mView.findViewById(R.id.dalle_image)
        val imageFrame: LinearLayout = mView.findViewById(R.id.image_frame)
        val btnCopy: ImageButton = mView.findViewById(R.id.btn_copy)

        btnCopy.setImageResource(R.drawable.ic_copy)

        btnCopy.setOnClickListener {
            val clipboard: ClipboardManager = mContext.getSystemService(FragmentActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("response", message.text.toString())
            clipboard.setPrimaryClip(clip)

            Toast.makeText(mContext, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        message.setTextIsSelectable(false)
        message.isClickable = false

        if (dataArray?.get(position)?.get("isBot") == true) {
            icon.setImageResource(R.drawable.assistant)
        } else {
            icon.setImageResource(R.drawable.ic_user)
        }

        if (dataArray?.get(position)?.get("message").toString().contains("data:image")) {
            imageFrame.visibility = View.VISIBLE
            message.visibility = View.GONE

            btnCopy.visibility = View.GONE

            val requestOptions = RequestOptions().transform(
                CenterCrop(),
                RoundedCorners(convertDpToPixel(24f, mContext).toInt())
            )

            Glide.with(mContext)
                .load(Uri.parse(dataArray?.get(position)?.get("message").toString()))
                .apply(requestOptions).into(dalleImage)

            dalleImage.setOnClickListener {
                val sharedPreferences: SharedPreferences =
                    mContext.getSharedPreferences("tmp", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("tmp", dataArray?.get(position)?.get("message").toString())
                editor.apply()

                val intent = Intent(mContext, ImageBrowserActivity::class.java)
                intent.putExtra("tmp", "1")
                mContext.startActivity(intent)
            }
        } else {
            val src = dataArray?.get(position)?.get("message").toString()

            val markwon: Markwon = Markwon.create(mContext)
            markwon.setMarkdown(message, src)

            imageFrame.visibility = View.GONE
            message.visibility = View.VISIBLE
        }

        return mView
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun getSurfaceColor(context: Context): Int {
        return SurfaceColors.SURFACE_2.getColor(context)
    }
}