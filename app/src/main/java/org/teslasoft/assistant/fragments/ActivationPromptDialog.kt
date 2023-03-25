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

class ActivationPromptDialog : DialogFragment() {
    companion object {
        public fun newInstance(prompt: String) : ActivationPromptDialog {
            val activationPromptDialog = ActivationPromptDialog()

            val args = Bundle()
            args.putString("prompt", prompt)

            activationPromptDialog.arguments = args

            return activationPromptDialog
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var promptInput: EditText? = null

    private var listener: StateChangesListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activation_prompt, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_activation_prompt, null)

        promptInput = view.findViewById(R.id.prompt_input)
        promptInput?.setText(requireArguments().getString("prompt"))

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ -> listener!!.onCanceled() }

        return builder!!.create()
    }

    private fun validateForm() {
        listener!!.onEdit(promptInput?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    public interface StateChangesListener {
        public fun onEdit(prompt: String)
        public fun onCanceled()
    }
}