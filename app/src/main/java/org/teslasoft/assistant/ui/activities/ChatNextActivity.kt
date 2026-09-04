/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.chat.ChatNextAdapter

class ChatNextActivity : FragmentActivity(), ChatNextAdapter.OnUpdateListener {

    private var btnBack: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var btnMessageAction: ImageButton? = null
    private var btnAttachFile: ImageButton? = null
    private var btnAttachPicture: ImageButton? = null
    private var btnOpenCamera: ImageButton? = null
    private var textChatName: TextView? = null
    private var fieldMessage: EditText? = null
    private var attachFileBox: ConstraintLayout? = null
    private var chatView: RecyclerView? = null
    private var assistantBusy: CircularProgressIndicator? = null
    private var bulkActionsBox: ConstraintLayout? = null
    private var btnSelectAll: ImageButton? = null
    private var btnDeselectAll: ImageButton? = null
    private var btnDeleteSelected: ImageButton? = null
    private var btnCopySelected: ImageButton? = null
    private var btnShareSelected: ImageButton? = null
    private var textSelectedMessagesCount: TextView? = null

    private val chatData: ArrayList<HashMap<String, Any>> = arrayListOf()
    private val selectedMessages: ArrayList<Boolean> = arrayListOf()
    private var chatAdapter: ChatNextAdapter? = null

    private var preferences: Preferences? = null

    private val testChatId = "next_ui_demo"

    private var assistantState: Int = 0 // 0 - default (show microphone button), 1 - type mode (show send button), 2 - busy (show loading indicator)
    private var isAttachFileBoxVisible: Boolean = false

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /**/ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_next)
        initializeViews()
        initializeViewListeners()
        testPrefillDatabase()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSettings = findViewById(R.id.btn_settings)
        btnMessageAction = findViewById(R.id.btn_message_action)
        btnAttachFile = findViewById(R.id.btn_attach_file)
        btnAttachPicture = findViewById(R.id.btn_attach_picture)
        btnOpenCamera = findViewById(R.id.btn_open_camera)
        textChatName = findViewById(R.id.text_chat_name)
        fieldMessage = findViewById(R.id.field_message)
        attachFileBox = findViewById(R.id.attach_file_box)
        chatView = findViewById(R.id.chat_view)
        assistantBusy = findViewById(R.id.assistant_busy)
        bulkActionsBox = findViewById(R.id.bulk_actions_box)
        btnSelectAll = findViewById(R.id.btn_select_all)
        btnDeselectAll = findViewById(R.id.btn_deselect_all)
        btnDeleteSelected = findViewById(R.id.btn_delete_selected)
        btnCopySelected = findViewById(R.id.btn_copy_selected)
        btnShareSelected = findViewById(R.id.btn_share_selected)
        textSelectedMessagesCount = findViewById(R.id.text_selected_messages_count)

        resetUiState()
    }

    private fun initializeViewListeners() {
        btnBack?.setOnClickListener {
            finishAfterTransition()
        }

        btnSettings?.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(btnSettings, ViewCompat.getTransitionName(btnSettings!!))
            )
            settingsLauncher.launch(
                Intent(this, SettingsActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", testChatId),
                options
            )
        }

        btnAttachFile?.setOnClickListener { if (isAttachFileBoxVisible) hideAttachFileBox() else showAttachFileBox() }

        btnMessageAction?.setOnClickListener {
            when (assistantState) {
                0 -> {
                    setAssistantBusy()
                    startRecording()
                }

                1 -> {
                    sendMessage()
                }

                2 -> {
                    updateActionButtonStateBasedOnMessageField(true)
                    stopAssistant()
                }
            }
        }

        btnAttachPicture?.setOnClickListener {
            hideAttachFileBox()
            attachPicture()
        }

        btnOpenCamera?.setOnClickListener {
            hideAttachFileBox()
            openCamera()
        }

        btnSelectAll?.setOnClickListener {
            chatAdapter?.selectAll()
        }

        btnDeselectAll?.setOnClickListener {
            chatAdapter?.unselectAll()
        }

        btnDeleteSelected?.setOnClickListener {

        }

        btnCopySelected?.setOnClickListener {

        }

        btnShareSelected?.setOnClickListener {

        }

        fieldMessage?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /**/ }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /**/}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateActionButtonStateBasedOnMessageField()
            }
        })
    }

    private fun updateActionButtonStateBasedOnMessageField(bypassBusyCheck: Boolean = false) {
        if (assistantState != 2 || bypassBusyCheck) {
            if (fieldMessage?.text?.toString()?.isEmpty() == true) {
                setMessageSpeakMode()
            } else {
                setMessageTypeMode()
            }
        }
    }

    private fun resetUiState() {
        hideAttachFileBox()
        setMessageSpeakMode()

        bulkActionsBox?.visibility = View.INVISIBLE
        bulkActionsBox?.translationY = -(bulkActionsBox?.height?.toFloat()?: 0f) - 100f
    }

    private fun showBulkActionsBoxAnimated() {
        bulkActionsBox?.visibility = View.VISIBLE
        bulkActionsBox?.animate()?.translationY(0f)?.setDuration(200)?.start()
    }

    private fun hideBulkActionsBoxAnimated() {
        bulkActionsBox?.animate()?.translationY(-(bulkActionsBox?.height?.toFloat()?: 0f) - 100f)?.setDuration(200)?.withEndAction {
            bulkActionsBox?.visibility = View.INVISIBLE
        }?.start()
    }

    private fun setMessageSpeakMode() {
        assistantState = 0
        btnMessageAction?.setImageResource(R.drawable.ic_microphone)
        assistantBusy?.hide()
    }

    private fun setMessageTypeMode() {
        assistantState = 1
        btnMessageAction?.setImageResource(R.drawable.ic_send)
        assistantBusy?.hide()
    }

    private fun setAssistantBusy() {
        assistantState = 2
        btnMessageAction?.setImageResource(R.drawable.ic_stop_recording)
        assistantBusy?.show()
    }

    private fun showAttachFileBox() {
        attachFileBox?.visibility = ConstraintLayout.VISIBLE
        isAttachFileBoxVisible = true
    }

    private fun hideAttachFileBox() {
        attachFileBox?.visibility = ConstraintLayout.GONE
        isAttachFileBoxVisible = false
    }

    private fun startRecording() {

    }

    private fun stopAssistant() {

    }

    private fun sendMessage() {

    }

    private fun attachPicture() {

    }

    private fun openCamera() {

    }

    private fun testPrefillDatabase() {
        chatData.clear()
        preferences = Preferences.getPreferences(this, testChatId)
        chatAdapter = ChatNextAdapter(chatData, selectedMessages, this, preferences ?: return, testChatId)
        chatAdapter?.setOnUpdateListener(this)

        chatView?.layoutManager = LinearLayoutManager(this)
        chatView?.adapter = chatAdapter

        putMessage("I need help with my account.", false)
        putMessage("Sure! Can you please provide me with your account number?", true)
        putMessage("Yes, it's 123456.", false)
        putMessage("Thank you! I will look into it and get back to you shortly.", true)
        putMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", false)
        putMessage("Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque faucibus ex sapien vitae pellentesque sem placerat. In id cursus mi pretium tellus duis convallis. Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos.\n" +
                "\n" +
                "Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque faucibus ex sapien vitae pellentesque sem placerat. In id cursus mi pretium tellus duis convallis. Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos.\n" +
                "\n" +
                "Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque faucibus ex sapien vitae pellentesque sem placerat. In id cursus mi pretium tellus duis convallis. Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos.", true)

        putMessage("Some message", false)
        putMessage("# Header 1\n\n## Header 2\n\n### Header 3\n\n*Italic text*\n\n> Quote\n\n- One\n- Two\n- Three\n\n```js\npublic static void myMethod(int param) {\n    // comment\n}\n```\nAn example of `selected` word.\n", true)

        putMessage("Some another message", false)
        putMessage("Example LaTeX math formula: \n\\[\n" +
                "A^2 + B^2 = C^2" +
                "\n\\]\n\nInlined formula example: \\(x = 2\\)", true)

        val content = assets.open("test_image_b64.txt").bufferedReader().use {
            it.readText()
        }

        putMessage("Some image request", false)
        putMessage("data:image/png;base64,$content", true)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun putMessage(message: String, isBot: Boolean, image: String = "", imageType: String = "") {
        val map: HashMap<String, Any> = HashMap()

        map["message"] = message
        map["isBot"] = isBot

        if (image != "") {
            map["image"] = image
            map["imageType"] = imageType
        }

        chatData.add(map)
        selectedMessages.add(false)
        chatAdapter?.notifyItemInserted(chatData.size - 1)
        scrollOnce()
    }

    private fun updateMessage(position: Int, message: String) {
        val map: HashMap<String, Any> = chatData[position]
        map["message"] = message
        chatAdapter?.notifyItemChanged(position)
    }

    private fun scrollOnce() {
        chatView?.post {
            chatView?.scrollTo(0, chatView?.bottom ?: 0)
        }
    }

    override fun onRetryClick() {

    }

    override fun onMessageEdited() {

    }

    override fun onMessageDeleted() {

    }

    override fun onBulkSelectionChanged(position: Int, selected: Boolean) {
        chatAdapter?.notifyItemChanged(position)
        textSelectedMessagesCount?.text = selectedMessages.stream().filter { it }.count().toString()
    }

    override fun onChangeBulkActionMode(mode: Boolean) {
        if (mode) {
            showBulkActionsBoxAnimated()
        } else {
            hideBulkActionsBoxAnimated()
        }
    }
}
