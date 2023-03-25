package org.teslasoft.assistant.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.ChatActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.adapters.ChatListAdapter
import org.teslasoft.assistant.onboarding.WelcomeActivity
import org.teslasoft.assistant.settings.SettingsActivity
import java.lang.reflect.Type

class ChatsListFragment : Fragment() {

    private var chats: ArrayList<HashMap<String, String>> = arrayListOf()

    private var adapter: ChatListAdapter? = null

    private var chatsList: ListView? = null

    private var btnSettings: ImageButton? = null

    private var btnAdd: ExtendedFloatingActionButton? = null

    var chatListUpdatedListener: AddChatDialogFragment.StateChangesListener = object : AddChatDialogFragment.StateChangesListener {
        override fun onAdd(name: String, id: String) {
            initSettings()

            val i = Intent(
                requireActivity(),
                ChatActivity::class.java
            )

            i.putExtra("name", name)
            i.putExtra("chatId", id)

            startActivity(i)
        }

        override fun onEdit(name: String, id: String) {
            initSettings()
        }

        override fun onError() {
            Toast.makeText(requireActivity(), "Please fill name field", Toast.LENGTH_SHORT).show()

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(this)
            chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
        }

        override fun onCanceled() {
            /* unused */
        }

        override fun onDelete() {
            initSettings()
        }

        override fun onDuplicate() {
            Toast.makeText(requireActivity(), "Name must be unique", Toast.LENGTH_SHORT).show()

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(this)
            chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatsList = view.findViewById(R.id.chats)
        btnSettings = view.findViewById(R.id.btn_settings_)

        btnSettings?.setImageResource(R.drawable.ic_settings)

        btnSettings?.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }

        btnAdd = view.findViewById(R.id.btn_add)

        btnAdd?.setOnClickListener {
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(chatListUpdatedListener)
            chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
        }

        adapter = ChatListAdapter(chats, this)

        chatsList?.dividerHeight = 0
        chatsList?.divider = null

        chatsList?.adapter = adapter

        adapter?.notifyDataSetChanged()

        preInit()
    }

    private fun preInit() {
        val settings: SharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        val key = settings.getString("api_key", null)

        if (key == null) {
            startActivity(Intent(requireActivity(), WelcomeActivity::class.java))
            requireActivity().finish()
        } else {
            initSettings()
        }
    }

    private fun initSettings() {
        val settings: SharedPreferences = requireActivity().getSharedPreferences("chat_list", Context.MODE_PRIVATE)

        chats = try {
            val gson = Gson()
            val json = settings.getString("data", null)
            val type: Type = object : TypeToken<ArrayList<HashMap<String, String>?>?>() {}.type

            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
        } catch (e: Exception) {
            arrayListOf()
        }

        adapter = ChatListAdapter(chats, this)
        chatsList?.adapter = adapter
        adapter?.notifyDataSetChanged()
    }
}