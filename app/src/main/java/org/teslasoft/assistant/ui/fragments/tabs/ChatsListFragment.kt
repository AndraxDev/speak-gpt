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

package org.teslasoft.assistant.ui.fragments.tabs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.activities.ChatActivity
import org.teslasoft.assistant.ui.activities.SettingsActivity
import org.teslasoft.assistant.ui.activities.SettingsV2Activity
import org.teslasoft.assistant.ui.adapters.ChatListAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.AddChatDialogFragment
import org.teslasoft.assistant.ui.onboarding.WelcomeActivity

import java.io.BufferedReader
import java.io.InputStreamReader

class ChatsListFragment : Fragment() {

    private var chats: ArrayList<HashMap<String, String>> = arrayListOf()

    private var adapter: ChatListAdapter? = null

    private var chatsList: ListView? = null

    private var btnSettings: ImageButton? = null

    private var btnAdd: ExtendedFloatingActionButton? = null

    private var btnImport: FloatingActionButton? = null

    private var bgSearch: ConstraintLayout? = null

    private var selectedFile: String = ""

    private var fieldSearch: EditText? = null

    private var searchTerm: String = ""

    var chatListUpdatedListener: AddChatDialogFragment.StateChangesListener = object : AddChatDialogFragment.StateChangesListener {
        override fun onAdd(name: String, id: String, fromFile: Boolean) {
            initSettings()

            if (fromFile && selectedFile.replace("null", "") != "") {
                val chat = requireActivity().getSharedPreferences("chat_$id", FragmentActivity.MODE_PRIVATE)
                val editor = chat.edit()

                editor.putString("chat", selectedFile)
                editor.apply()
            }

            val i = Intent(
                requireActivity(),
                ChatActivity::class.java
            ).setAction(Intent.ACTION_VIEW)

            i.putExtra("name", name)
            i.putExtra("chatId", id)

            startActivity(i)
        }

        override fun onEdit(name: String, id: String) {
            initSettings()
        }

        override fun onError(fromFile: Boolean) {
            Toast.makeText(requireActivity(), "Please fill name field", Toast.LENGTH_SHORT).show()

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("", fromFile, false, false)
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

            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("", false, false, false)
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

    override fun onResume() {
        super.onResume()
        initSettings()
    }

    fun reloadAmoled() {
        if (isDarkThemeEnabled() && Preferences.getPreferences(requireActivity(), "").getAmoledPitchBlack()) {
            btnSettings?.background = ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal_amoled, requireActivity().theme)!!
            bgSearch?.background = ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal_amoled, requireActivity().theme)!!
            btnImport?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.amoled_accent_100, requireActivity().theme)
            btnAdd?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_600, requireActivity().theme)
        } else {
            btnSettings?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal, requireActivity().theme)!!)
            bgSearch?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal, requireActivity().theme)!!)
            btnImport?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_250, requireActivity().theme)
            btnAdd?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_900, requireActivity().theme)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatsList = view.findViewById(R.id.chats)
        btnSettings = view.findViewById(R.id.btn_settings_)
        btnImport = view.findViewById(R.id.btn_import)
        fieldSearch = view.findViewById(R.id.field_search)
        bgSearch = view.findViewById(R.id.bg_search)

        btnSettings?.setImageResource(R.drawable.ic_settings)

        btnSettings?.setOnClickListener {
            if (Preferences.getPreferences(requireActivity(), "").getExperimentalUI()) {
                startActivity(Intent(requireActivity(), SettingsV2Activity::class.java).setAction(Intent.ACTION_VIEW))
            } else {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java).setAction(Intent.ACTION_VIEW))
            }
        }

        btnAdd = view.findViewById(R.id.btn_add)

        btnAdd?.setOnClickListener {
            val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance("", false, false, false)
            chatDialogFragment.setStateChangedListener(chatListUpdatedListener)
            chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
        }

        btnImport?.setOnClickListener {
            openFile(Uri.parse("/storage/emulated/0/chat.json"))
        }

        adapter = ChatListAdapter(chats, this)

        chatsList?.dividerHeight = 0
        chatsList?.divider = null

        chatsList?.adapter = adapter

        reloadAmoled()

        adapter?.notifyDataSetChanged()

        fieldSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchTerm = s.toString().trim()
                if (s.toString().trim() == "") {
                    adapter = ChatListAdapter(chats, this@ChatsListFragment)
                    chatsList?.adapter = adapter
                    adapter?.notifyDataSetChanged()
                } else {
                    val filtered: ArrayList<HashMap<String, String>> = arrayListOf()

                    for (i in chats) {
                        if (i["name"]?.contains(s.toString().trim()) == true || i["name"] == s.toString().trim()) {
                            filtered.add(i)
                        }
                    }

                    adapter = ChatListAdapter(filtered, this@ChatsListFragment)
                    chatsList?.adapter = adapter
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }

        })

        preInit()
    }

    private val fileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    selectedFile = readFile(uri)

                    if (isValidJson(selectedFile)) {
                        val chatDialogFragment: AddChatDialogFragment =
                            AddChatDialogFragment.newInstance("", true, false, false)
                        chatDialogFragment.setStateChangedListener(chatListUpdatedListener)
                        chatDialogFragment.show(
                            parentFragmentManager.beginTransaction(),
                            "AddChatDialog"
                        )
                    } else {
                        MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                            .setTitle("Error")
                            .setMessage("An error is occurred while analyzing file. The file might be corrupted or invalid.")
                            .setPositiveButton("Close") { _, _ -> }
                            .show()
                    }
                }
            }
        }
    }

    private fun isValidJson(jsonStr: String?): Boolean {
        return try {
            val gson = Gson()
            gson.fromJson(jsonStr, ArrayList::class.java)
            true
        } catch (ex: JsonSyntaxException) {
            false
        }
    }

    private fun readFile(uri: Uri) : String {
        val stringBuilder = StringBuilder()
        requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        fileIntentLauncher.launch(intent)
    }

    private fun preInit() {
        if (Preferences.getPreferences(requireActivity(), "").getApiKey(requireActivity()) == "") {
            if (Preferences.getPreferences(requireActivity(), "").getOldApiKey() == "") {
                requireActivity().getSharedPreferences("chat_list", Context.MODE_PRIVATE).edit().putString("data", "[]").apply()
                startActivity(Intent(requireActivity(), WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
                requireActivity().finish()
            } else {
                Preferences.getPreferences(requireActivity(), "").secureApiKey(requireActivity())
                initSettings()
            }
        } else {
            initSettings()
        }
    }

    private fun initSettings() {
        chats = ChatPreferences.getChatPreferences().getChatList(requireActivity())

        // R8 went fuck himself...
        if (chats == null) chats = arrayListOf()

        if (searchTerm.trim() == "") {
            adapter = ChatListAdapter(chats, this@ChatsListFragment)
            chatsList?.adapter = adapter
            adapter?.notifyDataSetChanged()
        } else {
            val filtered: ArrayList<HashMap<String, String>> = arrayListOf()

            for (i in chats) {
                if (i["name"]?.contains(searchTerm.trim()) == true || i["name"] == searchTerm.trim()) {
                    filtered.add(i)
                }
            }

            adapter = ChatListAdapter(filtered, this@ChatsListFragment)
            chatsList?.adapter = adapter
            adapter?.notifyDataSetChanged()
        }

        chatsList?.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) { /* unused */ }

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val topRowVerticalPosition: Int = if (chats.isEmpty() || chatsList == null || chatsList?.childCount == 0) 0 else chatsList?.getChildAt(0)!!.top

                if (firstVisibleItem == 0 && topRowVerticalPosition >= 0) {
                    btnAdd?.extend()
                } else {
                    btnAdd?.shrink()
                }
            }
        })
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && Preferences.getPreferences(requireActivity(), "").getAmoledPitchBlack()) {
            ResourcesCompat.getColor(requireActivity().resources, R.color.amoled_accent_100, requireActivity().theme)
        } else {
            SurfaceColors.SURFACE_5.getColor(requireActivity())
        }
    }
}
