package org.teslasoft.assistant.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import org.teslasoft.assistant.ChatActivity
import org.teslasoft.assistant.fragments.ChatsListFragment
import org.teslasoft.assistant.R
import org.teslasoft.assistant.fragments.AddChatDialogFragment
import org.teslasoft.assistant.util.Hash

class ChatListAdapter(data: ArrayList<HashMap<String, String>>?, context: Fragment) : BaseAdapter() {
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

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_chat_name, null)
        }

        val name: TextView = mView!!.findViewById(R.id.name)
        val selector: ConstraintLayout = mView.findViewById(R.id.chat_selector)
        val icon: ImageView = mView.findViewById(R.id.chat_icon)

        icon.setImageResource(R.drawable.chatgpt_icon)

        name.text = dataArray?.get(position)?.get("name").toString()

        if (position % 2 == 0) {
            selector.setBackgroundResource(R.drawable.btn_accent_selector_v2)
            icon.setBackgroundResource(R.drawable.btn_accent_tonal_v2)
        } else {
            selector.setBackgroundResource(R.drawable.btn_accent_selector)
        }

        selector.setOnClickListener {
            val i = Intent(
                    mContext.requireActivity(),
                    ChatActivity::class.java
            )

            i.putExtra("name", dataArray?.get(position)?.get("name").toString())
            i.putExtra("chatId", Hash.hash(dataArray?.get(position)?.get("name").toString()))

            mContext.requireActivity().startActivity(i)
        }

        selector.setOnLongClickListener {
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance(name.text.toString())
            chatDialogFragment.setStateChangedListener((mContext as ChatsListFragment).chatListUpdatedListener)
            chatDialogFragment.show(mContext.parentFragmentManager.beginTransaction(), "AddChatDialog")

            return@setOnLongClickListener true
        }

        return mView
    }
}