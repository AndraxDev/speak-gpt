/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.teslasoft.assistant.ui.PromptViewActivity
import org.teslasoft.assistant.R

class PromptAdapter(data: ArrayList<HashMap<String, String>>?, context: Fragment) : BaseAdapter() {
    private val dataArray: ArrayList<HashMap<String, String>>? = data
    private val mContext: Fragment = context

    override fun getCount(): Int {
        return dataArray!!.size
    }

    override fun getItem(position: Int): Any {
        return dataArray!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_prompt, null)
        }

        val background: LinearLayout = mView!!.findViewById(R.id.bg)
        val promptName: TextView = mView.findViewById(R.id.prompt_name)
        val promptDescription: TextView = mView.findViewById(R.id.prompt_description)
        val promptAuthor: TextView = mView.findViewById(R.id.prompt_author)
        val likesCounter: TextView = mView.findViewById(R.id.likes_count)
        val textFor: TextView = mView.findViewById(R.id.text_for)

        promptName.text = dataArray?.get(position)?.get("name")
        promptDescription.text = dataArray?.get(position)?.get("desc")
        promptAuthor.text = "By " + dataArray?.get(position)?.get("author")
        likesCounter.text = dataArray?.get(position)?.get("likes")
        textFor.text = dataArray?.get(position)?.get("type")

        background.setOnClickListener {
            val i = Intent(mContext.requireActivity(), PromptViewActivity::class.java)
            i.putExtra("id", dataArray?.get(position)?.get("id"))
            i.putExtra("title", dataArray?.get(position)?.get("name"))
            mContext.requireActivity().startActivity(i)
        }

        return mView
    }

}