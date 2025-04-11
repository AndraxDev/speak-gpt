/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.adapters.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.elevation.SurfaceColors
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlRenderer
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.activities.ImageBrowserActivity
import org.teslasoft.assistant.ui.fragments.dialogs.EditMessageDialogFragment
import org.teslasoft.assistant.util.StaticAvatarParser
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Base64
import java.util.Collections
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ChatAdapter(private val dataArray: ArrayList<HashMap<String, Any>>, private val selectorProjection: ArrayList<HashMap<String, Any>>, private val context: FragmentActivity, private val preferences: Preferences, private val isAssistant: Boolean, private var chatId: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder>(), EditMessageDialogFragment.StateChangesListener {

    private var dalleImageStringList = ArrayList<String>(Collections.nCopies(itemCount + 1, ""))
    private var imageStringList = ArrayList<String>(Collections.nCopies(itemCount + 1, ""))
    private var listener: OnUpdateListener? = null
    private var bulkActionMode = false

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
        private const val TYPE_CLASSIC = 2
    }

    fun setChatId(chatId: String) {
        this.chatId = chatId
    }

    override fun getItemViewType(position: Int): Int {
        return if (preferences.getLayout() == "bubbles" || isAssistant) {
            if (dataArray[position]["isBot"] == true) {
                TYPE_BOT
            } else {
                TYPE_USER
            }
        } else {
            TYPE_CLASSIC
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataArray[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (viewType) {
            TYPE_BOT -> R.layout.view_assistant_bot_message
            TYPE_USER -> R.layout.view_assistant_user_message
            else -> R.layout.view_message
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view, context)
    }

    fun setOnUpdateListener(listener: OnUpdateListener) {
        this.listener = listener
    }

    private fun editMessage(position: Int, message: String) {
        dataArray[position]["message"] = message
        listener?.onMessageEdited()
    }

    private fun deleteMessage(position: Int) {
        dataArray.removeAt(position)
        notifyItemRemoved(position)
        if (position > 0) {
            notifyItemRangeChanged(position - 1, itemCount)
        } else {
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    fun setBulkActionMode(bulkActionMode: Boolean) {
        this.bulkActionMode = bulkActionMode
    }

    private fun checkSelectionIsEmpty(): Boolean {
        var isEmpty = true

        for (projection in selectorProjection) {
            if (projection["selected"].toString() == "true") {
                isEmpty = false
                break
            }
        }

        return isEmpty
    }

    fun unselectAll() {
        for (projection in selectorProjection) {
            projection["selected"] = "false"
        }

        bulkActionMode = false
        listener?.onChangeBulkActionMode(false)
    }

    fun selectAll() {
        for (projection in selectorProjection) {
            projection["selected"] = "true"
        }

        bulkActionMode = true
        listener?.onChangeBulkActionMode(true)
    }

    open inner class ViewHolder(itemView: View, private val debugContext: Context) : RecyclerView.ViewHolder(itemView) {
        private val ui: ConstraintLayout = itemView.findViewById(R.id.ui)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val message: TextView = itemView.findViewById(R.id.message)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val bubbleBg: ConstraintLayout? = itemView.findViewById(R.id.bubble_bg)
        private val dalleImage: ImageView = itemView.findViewById(R.id.dalle_image)
        private val btnCopy: ImageButton = itemView.findViewById(R.id.btn_copy)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val btnRetry: ImageButton = itemView.findViewById(R.id.btn_retry)

        @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
        open fun bind(chatMessage: HashMap<String, Any>, position: Int) {

            updateUI(chatMessage)
            updateRetryButton(chatMessage, position)

            if (selectorProjection[position]["selected"].toString() == "true") {
                ui.setBackgroundColor(getSurface3Color(context))
            } else {
                updateUI(chatMessage)
            }

            ui.setOnLongClickListener {
                switchBulkActionState(position)
                return@setOnLongClickListener true
            }

            ui.setOnClickListener {
                if (bulkActionMode) {
                    switchBulkActionState(position)
                }
            }

            message.setOnLongClickListener {
                switchBulkActionState(position)
                return@setOnLongClickListener true
            }

            message.setOnClickListener {
                if (bulkActionMode) {
                    switchBulkActionState(position)
                }
            }

            btnEdit.setOnClickListener {
                if (!bulkActionMode) {
                    openEditDialog(chatMessage, position)
                }
            }

            btnCopy.setImageResource(R.drawable.ic_copy)
            btnCopy.setOnClickListener {
                val clipboard: ClipboardManager = context.getSystemService(FragmentActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("response", chatMessage["message"].toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, context.getString(R.string.label_copy), Toast.LENGTH_SHORT).show()
            }

            if (chatMessage["message"].toString().contains("data:image")) {
                dalleImage.visibility = View.VISIBLE
                message.visibility = View.GONE
                btnCopy.visibility = View.GONE

                loadImage(chatMessage["message"].toString())
                updateImageClickListener(chatMessage["message"].toString())
            } else if (chatMessage["message"].toString().contains("~file:")) {
                processFile(chatMessage, position, "png", dalleImageStringList, true)
            } else {
                (debugContext as FragmentActivity).runOnUiThread {
                    applyMarkdown(chatMessage)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    (debugContext as FragmentActivity).runOnUiThread {
                        applyMarkdown(chatMessage)
                    }
                }, 100)

                if (chatMessage["isBot"] == false && chatMessage["image"] !== null) {
                    dalleImage.visibility = View.VISIBLE

                    processFile(chatMessage, position, chatMessage["imageType"].toString(), imageStringList, false)
                } else {
                    dalleImage.visibility = View.GONE
                }

                message.visibility = View.VISIBLE
            }
        }

        private fun updateRetryButton(chatMessage: HashMap<String, Any>, position: Int) {
            if (dataArray.isNotEmpty() && position == dataArray.size - 1 && chatMessage["isBot"] == true) {
                btnRetry.visibility = View.VISIBLE

                btnRetry.setOnClickListener {
                    if (!bulkActionMode) {
                        listener?.onRetryClick()
                    }
                }
            } else {
                btnRetry.visibility = View.GONE
            }
        }

        private fun updateUI(chatMessage: HashMap<String, Any>) {
            if (preferences.getLayout() == "bubbles" || isAssistant) {
                updateBubbleLayout(chatMessage)
            } else {
                updateClassicLayout(chatMessage)
            }
        }

        private fun updateBubbleLayout(chatMessage: HashMap<String, Any>) {
            ui.setBackgroundColor(0x00000000)
            if (chatMessage["isBot"] == true) {
                displayAvatar()

                if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                    bubbleBg?.setBackgroundResource(R.drawable.bubble_out_dark)
                    message.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
                }
            } else {
                icon.setImageResource(R.drawable.ic_user)

                if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
                    bubbleBg?.setBackgroundResource(R.drawable.bubble_in_dark)
                    message.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
                }
            }
        }

        private fun updateClassicLayout(chatMessage: HashMap<String, Any>) {
            if (chatMessage["isBot"] == true) {
                displayAvatar()

                username.text = preferences.getAssistantName()
                ui.setBackgroundColor(getSurfaceColor(context))
            } else {
                icon.setImageResource(R.drawable.ic_user)
                username.text = context.getString(R.string.chat_role_user)
                btnCopy.visibility = View.VISIBLE
                ui.setBackgroundColor(getSurface2Color(context))
            }
        }

        private fun displayAvatar() {
            if (preferences.getAvatarType() == "builtin") {
                icon.setImageResource(StaticAvatarParser.parse(preferences.getAvatarId()))
                DrawableCompat.setTint(icon.getDrawable()!!, ContextCompat.getColor(context, R.color.accent_900))
            } else {
                readAndDisplay(Uri.fromFile(File(context.getExternalFilesDir("images")?.absolutePath + "/avatar_" + preferences.getAvatarId() + ".png")))
            }
        }

        private fun readAndDisplay(uri: Uri) {
            val bitmap = readFile(uri)

            if (bitmap != null) {
                icon.setImageBitmap(roundCorners(bitmap))
            }
        }

        private fun readFile(uri: Uri) : Bitmap? {
            return context.contentResolver?.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { _ ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
        }

        private fun roundCorners(bitmap: Bitmap): Bitmap {
            val output = createBitmap(bitmap.width, bitmap.height)
            val canvas = Canvas(output)

            val paint = Paint().apply {
                isAntiAlias = true
                color = -0xbdbdbe
            }

            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)

            canvas.drawRoundRect(rectF, 80f, 80f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        private fun switchBulkActionState(position: Int) {
            updateUI(dataArray[position])
            if (selectorProjection[position]["selected"].toString() == "true") {
                selectorProjection[position]["selected"] = "false"
                if (checkSelectionIsEmpty()) bulkActionMode = false
            } else {
                ui.setBackgroundColor(getSurface3Color(context))
                bulkActionMode = true
                selectorProjection[position]["selected"] = "true"
            }

            listener?.onBulkSelectionChanged(position, (selectorProjection[position]["selected"] ?: "false") == "true")
            listener?.onChangeBulkActionMode(bulkActionMode)
        }

        inner class BottomPaddingSpan(private val bottomPadding: Int) : LineHeightSpan {
            override fun chooseHeight(
                text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt?
            ) {
                fm?.let {
                    it.bottom += bottomPadding
                    it.descent += bottomPadding
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun applyMarkdown(chatMessage: HashMap<String, Any>) {
            if (chatMessage["isBot"] == true) {
                val src = chatMessage["message"].toString()
                val markwon: Markwon = Markwon.builder(context)
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(TablePlugin.create(context))
                    .usePlugin(TaskListPlugin.create(context))
                    .usePlugin(StrikethroughPlugin.create())
                    .usePlugin(MarkwonInlineParserPlugin.create())
                    .usePlugin(JLatexMathPlugin.create(message.textSize) { builder ->
                         builder.inlinesEnabled(true)
                    })
                    .usePlugin(object : AbstractMarkwonPlugin() {
                        override fun beforeSetText(
                            textView: TextView,
                            markdown: Spanned,
                        ) {
                            val spannableBuilder = SpannableStringBuilder(markdown)
                            val regex = Regex("\\|[^\\|]*\\|")
                            val matches = regex.findAll(spannableBuilder)

                            for (match in matches) {
                                val startIndex = match.range.first
                                val endIndex = match.range.last + 1
                                spannableBuilder.setSpan(
                                    BottomPaddingSpan(16),
                                    startIndex,
                                    endIndex,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            textView.text = spannableBuilder
                        }
                    })
                    .build()

                val pre = parseLatex(trimLineByLine(src))
                markwon.setMarkdown(message, pre)
            } else {
                message.text = chatMessage["message"].toString()
            }
        }

        private fun trimLineByLine(str: String) : String {
            val lines = str.split("\n")
            val sb = StringBuilder()
            for (line in lines) {
                sb.append(line.trim()).append("\n")
            }
            return sb.toString()
        }

        private fun parseLatex(markdown: String): String {
            val pattern = Regex("(`[^`]*`|```[\\s\\S]*?```)|\\\\\\[|\\\\\\]|\\\\\\(|\\\\\\)")
            // val pattern = Regex("(`[^`]*`|```[\\s\\S]*?```)|\\\\\\[|\\\\]|\\\\\\(|\\\\\\)")
            val sb = StringBuilder()
            var index = 0

            pattern.findAll(markdown).forEach { match ->
                if (match.groups[1] != null) { // Code block
                    sb.append(markdown.substring(index, match.range.first))
                    sb.append(match.value)
                    index = match.range.last + 1
                } else { // LaTeX \[, \], \(, or \) to be replaced
                    sb.append(markdown.substring(index, match.range.first))
                    when (match.value) {
                        """\[""" -> sb.append("""$$""").append("\n").append("""\[""")
                        """\]""" -> sb.append("""\]""").append("\n").append("""$$""")
                        """\(""" -> sb.append("""$$\(""")
                        """\)""" -> sb.append("""\)$$""")
                    }
                    index = match.range.last + 1
                }
            }
            sb.append(markdown.substring(index))

            val s = sb.toString()

            val openMatrixPattern = "\\begin{bmatrix}"
            val closeMatrixPattern = "\\end{bmatrix}"
            val openedMatricesCount = s.split(openMatrixPattern).size - 1
            val closedMatricesCount = s.split(closeMatrixPattern).size - 1

            val openMathPattern = "\\["
            val closeMathPattern = "\\]"
            val openedMathCount = s.split(openMathPattern).size - 1
            val closedMathCount = s.split(closeMathPattern).size - 1

            if (openedMatricesCount > closedMatricesCount) {
                sb.append("\\end{bmatrix}")
            }

            if (openedMathCount > closedMathCount) {
                sb.append("\n\\]")
            }

            return sb.toString()
        }

        @SuppressLint("SetTextI18n")
        private fun processFile(chatMessage: HashMap<String, Any>, position: Int, imageType: String, searchArray: ArrayList<String>, u: Boolean) {
            val mimeType = if (u || imageType == "png") "image/png" else "image/jpeg"

            val path = if(u) {
                chatMessage["message"].toString().replace("~file:", "")
            } else {
                chatMessage["image"]
            }

            try {
                val fullPath = context.getExternalFilesDir("images")?.absolutePath + "/" + path + "." + imageType

                while (searchArray.size < itemCount + 1) {
                    searchArray.add("")
                }

                if (searchArray[position] == "") {
                    context.contentResolver?.openFileDescriptor(
                        Uri.fromFile(
                            File(fullPath)
                        ), "r"
                    )?.use { file ->
                        FileInputStream(file.fileDescriptor).use { stream ->
                            run {
                                val c: ByteArray = stream.readBytes()
                                searchArray[position] = "data:$mimeType;base64," + Base64.getEncoder().encodeToString(c)
                                loadImage(searchArray[position])
                                updateImageClickListener(searchArray[position])
                            }
                        }
                    }
                } else {
                    loadImage(searchArray[position])
                    updateImageClickListener(searchArray[position])
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dalleImage.visibility = View.GONE
                message.visibility = View.VISIBLE
                btnCopy.visibility = View.VISIBLE
                message.text = "${message.text}\n<IMAGE NOT FOUND: $path.$mimeType>\nStacktrace: ${e.stackTraceToString()}"
            }
        }

        private fun loadImage(url: String) {
            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(convertDpToPixel(context).toInt()))
            Glide.with(context).load(url.toUri()).apply(requestOptions).into(dalleImage)
        }

        private fun updateImageClickListener(url: String) {
            dalleImage.setOnClickListener {
                if (bulkActionMode) {
                    switchBulkActionState(adapterPosition)
                } else {
                    val sharedPreferences: SharedPreferences = context.getSharedPreferences("tmp", Context.MODE_PRIVATE)
                    sharedPreferences.edit {
                        putString("tmp", url)
                    }
                    val intent = Intent(context, ImageBrowserActivity::class.java).setAction(Intent.ACTION_VIEW)
                    intent.putExtra("tmp", "1")
                    context.startActivity(intent)
                }
            }

            dalleImage.setOnLongClickListener {
                switchBulkActionState(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        private fun openEditDialog(chatMessage: HashMap<String, Any>, position: Int) {
            val dialog = EditMessageDialogFragment.newInstance(chatMessage["message"].toString(), position)
            dialog.setStateChangedListener(this@ChatAdapter)
            dialog.show(context.supportFragmentManager, "EditMessageDialogFragment")
        }

        fun resetView() {
            itemView.translationX = 0f
            itemView.alpha = 1f
        }
    }

    private fun convertDpToPixel(context: Context): Float {
        return 16f * context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    }

    private fun getSurfaceColor(context: Context): Int {
        return if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
            ResourcesCompat.getColor(context.resources, R.color.amoled_accent_50, null)
        } else {
            SurfaceColors.SURFACE_2.getColor(context)
        }
    }

    private fun getSurface2Color(context: Context): Int {
        return if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
            ResourcesCompat.getColor(context.resources, R.color.amoled_window_background, null)
        } else {
            SurfaceColors.SURFACE_1.getColor(context)
        }
    }

    private fun getSurface3Color(context: Context): Int {
        return if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
            ResourcesCompat.getColor(context.resources, R.color.amoled_accent_100, null)
        } else {
            SurfaceColors.SURFACE_4.getColor(context)
        }
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    override fun onEdit(prompt: String, position: Int) {
        editMessage(position, prompt)
        notifyItemChanged(position)

        if (chatId !== "") {
            ChatPreferences.getChatPreferences().editMessage(context, chatId, position, prompt)
        }
    }

    override fun onDelete(position: Int) {
        deleteMessage(position)

        if (chatId !== "") {
            ChatPreferences.getChatPreferences().deleteMessage(context, chatId, position)
        }

        listener?.onMessageDeleted()
    }

    interface OnUpdateListener {
        fun onRetryClick()
        fun onMessageEdited()
        fun onMessageDeleted()
        fun onBulkSelectionChanged(position: Int, selected: Boolean)
        fun onChangeBulkActionMode(mode: Boolean)
    }
}
