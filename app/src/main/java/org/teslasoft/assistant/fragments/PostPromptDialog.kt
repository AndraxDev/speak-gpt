package org.teslasoft.assistant.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R

class PostPromptDialog : DialogFragment() {
    companion object {
        public fun newInstance(name: String, title: String, desc: String, prompt: String) : PostPromptDialog {
            val postPromptDialog = PostPromptDialog()

            val args = Bundle()
            args.putString("name", name)
            args.putString("title", title)
            args.putString("desc", desc)
            args.putString("prompt", prompt)

            postPromptDialog.arguments = args

            return postPromptDialog
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var listener: StateChangesListener? = null

    private var fieldName: EditText? = null
    private var fieldTitle: EditText? = null
    private var fieldDesc: EditText? = null
    private var fieldPrompt: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post_prompt, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_post_prompt, null)

        fieldName = view.findViewById(R.id.field_author_name)
        fieldTitle = view.findViewById(R.id.field_prompt_title)
        fieldDesc = view.findViewById(R.id.field_prompt_desc)
        fieldPrompt = view.findViewById(R.id.field_prompt)

        fieldName?.setText(requireArguments().getString("name"))
        fieldTitle?.setText(requireArguments().getString("title"))
        fieldDesc?.setText(requireArguments().getString("desc"))
        fieldPrompt?.setText(requireArguments().getString("prompt"))

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("Post") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ -> listener!!.onCanceled() }

        return builder!!.create()
    }

    private fun validateForm() {
        if (fieldName?.text.toString().trim() == "" || fieldTitle?.text.toString().trim() == "" || fieldDesc?.text.toString().trim() == "" || fieldPrompt?.text.toString().trim() == "") {
            listener!!.onFormError(fieldName?.text.toString(), fieldTitle?.text.toString(), fieldDesc?.text.toString(), fieldPrompt?.text.toString())
        } else {
            listener!!.onFormFilled(fieldName?.text.toString(), fieldTitle?.text.toString(), fieldDesc?.text.toString(), fieldPrompt?.text.toString())
        }
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    public interface StateChangesListener {
        public fun onFormFilled(name: String, title: String, desc: String, prompt: String)

        public fun onFormError(name: String, title: String, desc: String, prompt: String)
        public fun onCanceled()
    }
}