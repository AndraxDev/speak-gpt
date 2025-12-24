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

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.transition.TransitionInflater
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.util.Hash
import org.teslasoft.assistant.util.StaticAvatarParser
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import androidx.core.net.toUri
import androidx.core.graphics.createBitmap

class CustomizeAssistantDialog : DialogFragment() {
    companion object {
        fun newInstance(chatId: String, name: String, avatarType: String, avatarId: String) : CustomizeAssistantDialog {
            val customizeAssistantDialog = CustomizeAssistantDialog()

            val args = Bundle()
            args.putString("chatId", chatId)
            args.putString("name", name)
            args.putString("avatarType", avatarType)
            args.putString("avatarId", avatarId)

            customizeAssistantDialog.arguments = args

            return customizeAssistantDialog
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var textDialogTitle: TextView? = null
    private var fieldAssistantName: TextInputEditText? = null
    private var btnView1: ImageButton? = null
    private var btnView2: ImageButton? = null
    private var btnView3: ImageButton? = null
    private var btnView4: ImageButton? = null
    private var btnView5: ImageButton? = null
    private var btnSelectFile: MaterialButton? = null
    private var previewFile: ImageView? = null

    private var selectedAvatarType = "builtin"
    private var selectedAvatarId = "gpt"
    private var bitmap: Bitmap? = null
    private var baseImageString: String? = null
    private var selectedImageType: String? = null

    private var listener: CustomizeAssistantDialogListener? = null

    fun setCustomizeAssistantDialogListener(listener: CustomizeAssistantDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_customize, container, false)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        return view
    }

    private val fileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    readAndDisplay(uri, true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the shared element transitions
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_customize, null)

        textDialogTitle = view.findViewById(R.id.text_dialog_title_cust)
        fieldAssistantName = view.findViewById(R.id.field_assistant_name)
        btnView1 = view.findViewById(R.id.view1)
        btnView2 = view.findViewById(R.id.view2)
        btnView3 = view.findViewById(R.id.view3)
        btnView4 = view.findViewById(R.id.view4)
        btnView5 = view.findViewById(R.id.view5)
        btnSelectFile = view.findViewById(R.id.btn_file_select)
        previewFile = view.findViewById(R.id.current_avatar_cust)

        btnView1?.setImageResource(R.drawable.chatgpt_icon)
        DrawableCompat.setTint(btnView1?.getDrawable()!!, ContextCompat.getColor(requireActivity(), R.color.accent_900))

        btnView2?.setImageResource(R.drawable.google_bard)
        DrawableCompat.setTint(btnView2?.getDrawable()!!, ContextCompat.getColor(requireActivity(), R.color.accent_900))

        btnView3?.setImageResource(R.drawable.perplexity_ai)
        DrawableCompat.setTint(btnView3?.getDrawable()!!, ContextCompat.getColor(requireActivity(), R.color.accent_900))

        fieldAssistantName?.setText(requireArguments().getString("name"))

        selectedAvatarType = requireArguments().getString("avatarType")!!
        selectedAvatarId = requireArguments().getString("avatarId")!!

        if (requireArguments().getString("avatarType") == "builtin") {
            previewFile?.setImageResource(StaticAvatarParser.parse(requireArguments().getString("avatarId")!!))
            DrawableCompat.setTint(previewFile?.getDrawable()!!, ContextCompat.getColor(requireActivity(), R.color.accent_900))
        } else if (requireArguments().getString("avatarType") == "file") {
            readAndDisplay(Uri.fromFile(File(requireActivity().getExternalFilesDir("images")?.absolutePath + "/avatar_" + requireArguments().getString("avatarId")!! + ".png")))
        }

        btnView1?.setOnClickListener {
            previewFile?.setImageResource(R.drawable.chatgpt_icon)
            selectedAvatarType = "builtin"
            selectedAvatarId = "gpt"
        }

        btnView2?.setOnClickListener {
            previewFile?.setImageResource(R.drawable.google_bard)
            selectedAvatarType = "builtin"
            selectedAvatarId = "gemini"
        }

        btnView3?.setOnClickListener {
            previewFile?.setImageResource(R.drawable.perplexity_ai)
            selectedAvatarType = "builtin"
            selectedAvatarId = "perplexity"
        }

        btnView4?.setOnClickListener {
            // TODO: Wait for a better times...
        }

        btnView5?.setOnClickListener {
            previewFile?.setImageResource(R.drawable.assistant)
            selectedAvatarType = "builtin"
            selectedAvatarId = "speakgpt"
        }

        btnSelectFile?.setOnClickListener {
            openFile("/storage/emulated/0/image.png".toUri())
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> run {
                if (fieldAssistantName?.text.toString().isNotEmpty()) {
                    listener?.onEdit(fieldAssistantName?.text.toString(), selectedAvatarType, selectedAvatarId)
                } else {
                    listener?.onEdit(getString(R.string.app_name), selectedAvatarType, selectedAvatarId)
                }
            }}
            .setNegativeButton(R.string.btn_cancel) { _, _ -> listener?.onCancel() }

        val dialog = builder!!.create()

        dialog.window?.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        return dialog
    }

    private fun readFile(uri: Uri) : Bitmap? {
        return requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { _ ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        fileIntentLauncher.launch(intent)
    }

    private fun readAndDisplay(uri: Uri, write: Boolean = false) {
        bitmap = readFile(uri)

        if (bitmap != null) {
            previewFile?.setImageBitmap(roundCorners(bitmap ?: return))

            val mimeType = requireActivity().contentResolver.getType(uri)
            val format = when {
                mimeType.equals("image/png", ignoreCase = true) -> {
                    selectedImageType = "png"
                    Bitmap.CompressFormat.PNG
                }
                else -> {
                    selectedImageType = "jpg"
                    Bitmap.CompressFormat.JPEG
                }
            }

            // Step 3: Convert the Bitmap to a Base64-encoded string
            val outputStream = ByteArrayOutputStream()
            bitmap!!.compress(format, 100, outputStream) // Note: Adjust the quality as necessary
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            // Step 4: Generate the data URL
            val imageType = when(format) {
                Bitmap.CompressFormat.JPEG -> "jpeg"
                Bitmap.CompressFormat.PNG -> "png"
                // Add more mappings as necessary
                else -> ""
            }

            baseImageString = "data:image/$imageType;base64,$base64Image"

            if (write) {
                val bytes = Base64.decode(baseImageString!!.split(",")[1], Base64.DEFAULT)
                writeImageToCache(bytes, selectedImageType!!)
            }
        }
    }

    private fun writeImageToCache(bytes: ByteArray, imageType: String = "png") {
        val avid = Hash.hash(java.util.Base64.getEncoder().encodeToString(bytes))
        selectedAvatarId = avid
        selectedAvatarType = "file"
        try {
            requireActivity().contentResolver.openFileDescriptor(Uri.fromFile(File(requireActivity().getExternalFilesDir("images")?.absolutePath + "/avatar_" + avid + "." + imageType)), "w")?.use { fileDescriptor ->
                FileOutputStream(fileDescriptor.fileDescriptor).use {
                    it.write(
                        bytes
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun roundCorners(bitmap: Bitmap): Bitmap {
        // Create a bitmap with the same size as the original.
        val output = createBitmap(bitmap.width, bitmap.height)

        // Prepare a canvas with the new bitmap.
        val canvas = Canvas(output)

        // The paint used to draw the original bitmap onto the new one.
        val paint = Paint().apply {
            isAntiAlias = true
            color = -0xbdbdbe
        }

        // The rectangle bounds for the original bitmap.
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        // Draw rounded rectangle as background.
        canvas.drawRoundRect(rectF, 80f, 80f, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    interface CustomizeAssistantDialogListener {
        fun onEdit(assistantName: String, avatarType: String, avatarId: String)
        fun onError(assistantName: String, avatarType: String, avatarId: String, error: String, dialog: CustomizeAssistantDialog)
        fun onCancel()
    }
}
