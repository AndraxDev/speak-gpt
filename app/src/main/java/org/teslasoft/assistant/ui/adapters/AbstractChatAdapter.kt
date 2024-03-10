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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.elevation.SurfaceColors

import io.noties.markwon.Markwon

import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.activities.ImageBrowserActivity

import java.io.File
import java.io.FileInputStream
import java.util.Base64

abstract class AbstractChatAdapter(data: ArrayList<HashMap<String, Any>>?, context: FragmentActivity) : BaseAdapter() {
    protected val dataArray: ArrayList<HashMap<String, Any>>? = data
    protected val mContext: FragmentActivity = context
    override fun getCount(): Int {
        return dataArray!!.size
    }
    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    protected var ui: ConstraintLayout? = null
    protected var icon: ImageView? = null
    protected var message: TextView? = null
    protected var dalleImage: ImageView? = null
    protected var btnCopy: ImageButton? = null

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        btnCopy?.setImageResource(R.drawable.ic_copy)
        btnCopy?.setOnClickListener {
            val clipboard: ClipboardManager = mContext.getSystemService(FragmentActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("response", dataArray?.get(position)?.get("message").toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(mContext, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        if (dataArray?.get(position)?.get("message").toString().contains("data:image")) {
            dalleImage?.visibility = View.VISIBLE
            message?.visibility = View.GONE
            btnCopy?.visibility = View.GONE

            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(convertDpToPixel(16f, mContext).toInt()))
            Glide.with(mContext).load(Uri.parse(dataArray?.get(position)?.get("message").toString())).apply(requestOptions).into(dalleImage!!)

            dalleImage?.setOnClickListener {
                val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("tmp", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("tmp", dataArray?.get(position)?.get("message").toString())
                editor.apply()
                val intent = Intent(mContext, ImageBrowserActivity::class.java).setAction(Intent.ACTION_VIEW)
                intent.putExtra("tmp", "1")
                mContext.startActivity(intent)
            }
        } else if (dataArray?.get(position)?.get("message").toString().contains("~file:")) {
            dalleImage?.visibility = View.VISIBLE
            message?.visibility = View.GONE
            btnCopy?.visibility = View.GONE


            val contents: String = dataArray?.get(position)?.get("message").toString()
            val fileName: String = contents.replace("~file:", "")
            try {
                val fullPath = mContext.getExternalFilesDir("images")?.absolutePath + "/" + fileName + ".png"
                mContext.contentResolver.openFileDescriptor(Uri.fromFile(
                    File(fullPath))
                    , "r")?.use {
                    FileInputStream(it.fileDescriptor).use {
                        val c: ByteArray = it.readBytes()
                        val m: String = "data:image/png;base64," + Base64.getEncoder().encodeToString(c)

                        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(convertDpToPixel(16f, mContext).toInt()))
                        Glide.with(mContext).load(Uri.parse(m)).apply(requestOptions).into(dalleImage!!)

                        dalleImage?.setOnClickListener {
                            val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("tmp", Context.MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("tmp", m)
                            editor.apply()
                            val intent = Intent(mContext, ImageBrowserActivity::class.java).setAction(Intent.ACTION_VIEW)
                            intent.putExtra("tmp", "1")
                            mContext.startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dalleImage?.visibility = View.GONE
                message?.visibility = View.VISIBLE
                btnCopy?.visibility = View.VISIBLE
                message?.text = "<FILE NOT FOUND>"
            }
        } else {
            if (dataArray?.get(position)?.get("isBot") == true) {
                val src = dataArray[position]["message"].toString()
                val markwon: Markwon = Markwon.create(mContext)
                markwon.setMarkdown(message!!, src)
            } else {
                message?.text = dataArray?.get(position)?.get("message").toString()
            }

            dalleImage?.visibility = View.GONE
            message?.visibility = View.VISIBLE
        }

        return convertView!!
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    protected fun getSurface3Drawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), get3SurfaceColor(context))
        return drawable
    }

    protected fun getSurfaceColor(context: Context): Int {
        return SurfaceColors.SURFACE_2.getColor(context)
    }

    private fun get3SurfaceColor(context: Context): Int {
        return SurfaceColors.SURFACE_3.getColor(context)
    }
}
