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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.Hash

class AddChatDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(name: String, fromFile: Boolean) : AddChatDialogFragment {
            val addChatDialogFragment = AddChatDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putBoolean("fromFile", fromFile)

            addChatDialogFragment.arguments = args

            return addChatDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var nameInput: EditText? = null

    private var listener: StateChangesListener? = null

    private var isEdit = false

    private var chatPreferences: ChatPreferences? = null

    private var autoName: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_chat, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        chatPreferences = ChatPreferences.getChatPreferences()

        val view: View = this.layoutInflater.inflate(R.layout.fragment_add_chat, null)

        nameInput = view.findViewById(R.id.field_name)
        autoName = view.findViewById(R.id.auto_name)

        val dialogTitle: TextView = view.findViewById(R.id.dialog_title)

        if (requireArguments().getString("name") != "") {
            dialogTitle.text = requireActivity().resources.getString(R.string.title_edit_chat)

            nameInput?.setText(requireArguments().getString("name"))

            builder!!.setView(view)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> validateForm() }
                    .setNeutralButton("Delete") { _, _ -> confirmDeletion(requireActivity()) }
                    .setNegativeButton("Cancel") { _, _ -> listener!!.onCanceled() }

            isEdit = true

            autoName?.visibility = View.GONE
        } else {
            dialogTitle.text = requireActivity().resources.getString(R.string.title_new_chat)

            nameInput?.setText("New chat ${chatPreferences?.getAvailableChatId(requireActivity())}")

            autoName?.setOnCheckedChangeListener { _, isChecked -> run {
                nameInput?.isEnabled = !isChecked
            } }

            builder!!.setView(view)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> validateForm() }
                    .setNegativeButton("Cancel") { _, _: Int -> listener!!.onCanceled() }

            autoName?.visibility = View.VISIBLE
        }

        if (arguments?.getBoolean("fromFile")!!) autoName?.visibility = View.GONE

        return builder!!.create()
    }

    private fun validateForm() {
        if (nameInput?.text.toString() == "" && !autoName!!.isChecked) {
            listener!!.onError(arguments?.getBoolean("fromFile") == true)
        } else {
            createChat()
        }
    }

    private fun createChat() {
        if (chatPreferences?.checkDuplicate(requireActivity(), nameInput?.text.toString()) == false) {
            val chatName = if (autoName?.isChecked!!) "_autoname_${chatPreferences?.getAvailableChatIdForAutoname(requireActivity())}" else nameInput?.text.toString()

            val preferences: Preferences = if (isEdit) {
                chatPreferences?.editChat(requireActivity(), nameInput?.text.toString(), requireArguments().getString("name").toString())
                listener!!.onEdit(chatName, Hash.hash(nameInput?.text.toString()))

                // Transfer settings
                Preferences.getPreferences(requireActivity(), Hash.hash(arguments?.getString("name").toString()))
            } else {
                chatPreferences?.addChat(requireActivity(), chatName)
                listener!!.onAdd(chatName, Hash.hash(chatName), arguments?.getBoolean("fromFile") == true)

                // Copy settings from default
                Preferences.getPreferences(requireActivity(), "")
            }

            // Write settings
            val resolution = preferences.getResolution()
            val speech = preferences.getAudioModel()
            val model = preferences.getModel()
            val maxTokens = preferences.getMaxTokens()
            val prefix = preferences.getPrefix()
            val endSeparator = preferences.getEndSeparator()
            val activationPrompt = preferences.getPrompt()
            val layout = preferences.getLayout()
            val silent = preferences.getSilence()
            val systemMessage = preferences.getSystemMessage()
            val alwaysSpeak = preferences.getNotSilence()
            val autoLanguageDetect = preferences.getAutoLangDetect()
            val functionCalling = preferences.getFunctionCalling()
            val slashCommands = preferences.getImagineCommand()
            val ttsEngine = preferences.getTtsEngine()

            preferences.setPreferences(Hash.hash(chatName), requireActivity())
            preferences.setResolution(resolution)
            preferences.setAudioModel(speech)
            preferences.setModel(model)
            preferences.setMaxTokens(maxTokens)
            preferences.setPrefix(prefix)
            preferences.setEndSeparator(endSeparator)
            preferences.setPrompt(activationPrompt)
            preferences.setLayout(layout)
            preferences.setSilence(silent)
            preferences.setSystemMessage(systemMessage)
            preferences.setNotSilence(alwaysSpeak)
            preferences.setAutoLangDetect(autoLanguageDetect)
            preferences.setFunctionCalling(functionCalling)
            preferences.setImagineCommand(slashCommands)
            preferences.setTtsEngine(ttsEngine)
        } else {
            listener!!.onDuplicate()
        }
    }

    private fun confirmDeletion(context: Context) {
        MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                .setTitle("Confirm deletion")
                .setMessage("This action can not be undone.")
                .setPositiveButton("Delete") { _, _ -> delete(context) }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
    }

    private fun delete(context: Context) {
        chatPreferences?.deleteChat(context, requireArguments().getString("name").toString())
        listener!!.onDelete()
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onAdd(name: String, id: String, fromFile: Boolean)
        fun onEdit(name: String, id: String)
        fun onError(fromFile: Boolean)
        fun onCanceled()
        fun onDelete()
        fun onDuplicate()
    }
}
