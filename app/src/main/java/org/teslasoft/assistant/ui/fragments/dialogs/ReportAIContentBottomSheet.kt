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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity.RESULT_OK
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ReportAIContentBottomSheet : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(message: String, chatId: String, reportFromChat: Boolean, playgroundUserMessage: String = ""): ReportAIContentBottomSheet {
            val fragment = ReportAIContentBottomSheet()
            val args = Bundle()
            args.putString("message", message)
            args.putString("chatId", chatId)
            args.putBoolean("reportFromChat", reportFromChat)
            args.putString("playgroundUserMessage", playgroundUserMessage)
            fragment.arguments = args
            return fragment
        }
    }

    private var optionMessage: RadioButton? = null
    private var optionEntireChat: RadioButton? = null
    private var fieldAdditionalInfo: TextInputEditText? = null
    private var optionOpenAI: RadioButton? = null
    private var optionExport: RadioButton? = null
    private var optionShare: RadioButton? = null
    private var openRouterDiscord: MaterialButton? = null
    private var groqDiscord: MaterialButton? = null
    private var groqConsole: MaterialButton? = null
    private var btnSend: MaterialButton? = null
    private var btnCancel: MaterialButton? = null
    private var fileContents = ByteArray(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionMessage = view.findViewById(R.id.option_message_only)
        optionEntireChat = view.findViewById(R.id.option_entire_chat)
        fieldAdditionalInfo = view.findViewById(R.id.field_additional_info)
        optionOpenAI = view.findViewById(R.id.option_openai)
        optionExport = view.findViewById(R.id.option_export)
        optionShare = view.findViewById(R.id.option_share)
        openRouterDiscord = view.findViewById(R.id.openrouter_discord)
        groqDiscord = view.findViewById(R.id.groq_discord)
        groqConsole = view.findViewById(R.id.groq_console)
        btnSend = view.findViewById(R.id.btn_send)
        btnCancel = view.findViewById(R.id.btn_cancel)

        btnSend?.setOnClickListener {
            validateForm()
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }

        openRouterDiscord?.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = "https://discord.com/invite/fVyRaUDgxW".toUri()
            }.also {
                startActivity(it)
            }
        }

        groqDiscord?.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = "https://discord.com/invite/SU35ycSbAb".toUri()
            }.also {
                startActivity(it)
            }
        }

        groqConsole?.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = "https://console.groq.com/".toUri()
            }.also {
                startActivity(it)
            }
        }
    }

    private fun validateForm() {
        val message: String? = arguments?.getString("message")
        val chatId: String? = arguments?.getString("chatId")
        val contactEmail = if (optionOpenAI?.isChecked == true) "trustandsafety@openai.com" else ""
        val context = requireContext()

        if (optionMessage?.isChecked == false && optionEntireChat?.isChecked == false) {
            Toast.makeText(context, "Please select what you want to report", Toast.LENGTH_SHORT).show()
            return
        }

        if (optionOpenAI?.isChecked == false && optionExport?.isChecked == false && optionShare?.isChecked == false) {
            Toast.makeText(context, "Please select how you want to report", Toast.LENGTH_SHORT).show()
            return
        }

        val preferences = Preferences.getPreferences(context, chatId.toString())
        val apiEndpointPreferences: ApiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(context)
        val apiEndpointHost = apiEndpointPreferences.getApiEndpoint(context, preferences.getApiEndpointId()).host
        val model = preferences.getModel()
        val reportedContent = if (optionMessage?.isChecked == true) {
            "\nMessage: \n$message"
        } else {
            "\nChat: \n${chatConversationToText(context, chatId.toString())}"
        }
        val composedReport = trimLineByLine("""
            Reported Content: $reportedContent
            Additional Info: ${fieldAdditionalInfo?.text.toString()}
            AI Model: $model
            API Endpoint host: $apiEndpointHost
        """.trimIndent())

        if (optionOpenAI?.isChecked == true) {
            Intent(Intent.ACTION_SENDTO).apply {
                val uri = "mailto:$contactEmail".toUri()
                    .buildUpon()
                    .appendQueryParameter("subject", "Report AI-generated Content")
                    .appendQueryParameter("body", composedReport)
                    .build()
                data = uri
                putExtra(Intent.EXTRA_SUBJECT, "Report AI-generated Content")
                putExtra(Intent.EXTRA_TEXT, composedReport)
            }.also {
                val chooser = Intent.createChooser(it, "Report AI-generated Content")
                startActivity(chooser)
            }
        } else if (optionExport?.isChecked == true) {
            fileContents = composedReport.toByteArray()

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "report.txt")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, (Environment.getExternalStorageDirectory().path + "/SpeakGPT/report.txt").toUri())
            }
            fileSaveIntentLauncher.launch(intent)
        } else if (optionShare?.isChecked == true) {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, composedReport)
            }.also {
                val chooser = Intent.createChooser(it, "Share Report")
                startActivity(chooser)
            }
        }
    }

    private fun trimLineByLine(text: String): String {
        val stringBuilder = StringBuilder()
        val lines = text.split("\n")
        for (line in lines) {
            stringBuilder.append(line.trim())
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private val fileSaveIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.also { uri ->
                    writeToFile(uri)
                }
            }
        }
    }

    private fun writeToFile(uri: Uri) {
        try {
            requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { stream ->
                    stream.write(
                        fileContents
                    )
                }
            }
            Toast.makeText(requireActivity(), "Report saved to text file", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Toast.makeText(requireActivity(), "Save failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(requireActivity(), "Save failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun chatConversationToText(context: Context, chatId: String) : String {
        val reportFromChat = arguments?.getBoolean("reportFromChat") ?: false
        if (reportFromChat) {
            val messagesSelectionProjection =
                ChatPreferences.getChatPreferences().getChatById(context, chatId)
            val stringBuilder = StringBuilder()

            for (m in messagesSelectionProjection) {
                if (m["isBot"] == true) {
                    stringBuilder.append("[Bot] >\n")
                } else {
                    stringBuilder.append("[User] >\n")
                }
                stringBuilder.append(m["message"])
                stringBuilder.append("\n")
            }

            return stringBuilder.toString()
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder.append("[User] >\n")
            stringBuilder.append(arguments?.getString("playgroundUserMessage"))
            stringBuilder.append("\n")
            stringBuilder.append("[Bot] >\n")
            stringBuilder.append(arguments?.getString("message"))
            stringBuilder.append("\n")
            return stringBuilder.toString()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
    }
}
