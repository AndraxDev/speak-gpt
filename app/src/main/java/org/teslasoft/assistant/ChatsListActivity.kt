package org.teslasoft.assistant

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.adapters.ChatListAdapter
import org.teslasoft.assistant.fragments.AddChatDialogFragment
import org.teslasoft.assistant.onboarding.WelcomeActivity
import org.teslasoft.assistant.settings.SettingsActivity
import java.lang.reflect.Type

class ChatsListActivity : FragmentActivity() {

    private var chats: ArrayList<HashMap<String, String>> = arrayListOf()

    private var adapter: ChatListAdapter? = null

    private var chatsList: ListView? = null

    private var btnSettings: ImageButton? = null

    private var btnAdd: ExtendedFloatingActionButton? = null

    var chatListUpdatedListener: AddChatDialogFragment.StateChangesListener = object : AddChatDialogFragment.StateChangesListener {
        override fun onEdit() {
            initSettings()
        }

        override fun onError() {
            Toast.makeText(this@ChatsListActivity, "Error", Toast.LENGTH_SHORT).show()

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(this)
            chatDialogFragment.show(supportFragmentManager.beginTransaction(), "AddChatDialog")
        }

        override fun onCanceled() {
            /* unused */
        }

        override fun onDelete() {
            initSettings()
        }

        override fun onDuplicate() {
            Toast.makeText(this@ChatsListActivity, "Name must be unique", Toast.LENGTH_SHORT).show()

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(this)
            chatDialogFragment.show(supportFragmentManager.beginTransaction(), "AddChatDialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chats_list)

        chatsList = findViewById(R.id.chats)
        btnSettings = findViewById(R.id.btn_settings_)

        btnSettings?.setImageResource(R.drawable.ic_settings)

        btnSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnAdd = findViewById(R.id.btn_add)

        btnAdd?.setOnClickListener {
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("")
            chatDialogFragment.setStateChangedListener(chatListUpdatedListener)
            chatDialogFragment.show(supportFragmentManager.beginTransaction(), "AddChatDialog")
        }

        adapter = ChatListAdapter(chats, this)

        chatsList?.dividerHeight = 0
        chatsList?.divider = null

        chatsList?.adapter = adapter

        adapter?.notifyDataSetChanged()

        preInit()
    }

    private fun preInit() {
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        val key = settings.getString("api_key", null)

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            initSettings()
        }
    }

    private fun initSettings() {
        val settings: SharedPreferences = getSharedPreferences("chat_list", MODE_PRIVATE)

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