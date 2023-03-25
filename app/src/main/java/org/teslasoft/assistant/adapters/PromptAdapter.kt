package org.teslasoft.assistant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_prompt, null)
        }

        val bacground: LinearLayout = mView!!.findViewById(R.id.bg)
        val promptName: TextView = mView.findViewById(R.id.prompt_name)
        val promptDescription: TextView = mView.findViewById(R.id.prompt_description)
        val promptAuthor: TextView = mView.findViewById(R.id.prompt_author)
        val likesCounter: TextView = mView.findViewById(R.id.likes_count)

        promptName.text = dataArray?.get(position)?.get("name")
        promptDescription.text = dataArray?.get(position)?.get("desc")
        promptAuthor.text = dataArray?.get(position)?.get("author")
        likesCounter.text = dataArray?.get(position)?.get("likes")

        bacground.setOnClickListener {

        }

        return mView
    }

}