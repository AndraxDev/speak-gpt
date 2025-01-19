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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.R

class LogitBiasConfigItemAdapter(private val dataArray: ArrayList<HashMap<String, String>>, private var mContext: Context) : BaseAdapter() {
    override fun getCount(): Int {
        return dataArray.size
    }

    override fun getItem(position: Int): Any {
        return dataArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private var ui: ConstraintLayout? = null
    private var configLabel: TextView? = null
    private var tokensCount: TextView? = null
    private var btnEditLogitBiases: MaterialButton? = null

    private var listener: OnSelectListener? = null

    fun setOnSelectListener(listener: OnSelectListener) {
        this.listener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_logit_bias_config_item, null)
        }

        ui = mView?.findViewById(R.id.ui)
        configLabel = mView?.findViewById(R.id.config_label)
        tokensCount = mView?.findViewById(R.id.tokens_count)
        btnEditLogitBiases = mView?.findViewById(R.id.btn_edit_biases)

        val item = dataArray[position]

        configLabel?.text = item["label"]

        ui?.setOnClickListener {
            listener?.onClick(position)
        }

        ui?.setOnLongClickListener {
            listener?.onLongClick(position)
            return@setOnLongClickListener true
        }

        btnEditLogitBiases?.setOnClickListener {
            listener?.onEditBiases(position)
        }

        if (position == 0) {
            btnEditLogitBiases?.visibility = View.GONE
        } else {
            btnEditLogitBiases?.visibility = View.VISIBLE
        }

        return mView!!
    }

    interface OnSelectListener {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
        fun onEditBiases(position: Int)
    }
}