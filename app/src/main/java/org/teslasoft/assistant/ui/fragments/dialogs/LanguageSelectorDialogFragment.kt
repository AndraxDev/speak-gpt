package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R

class LanguageSelectorDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(name: String, chatId: String) : LanguageSelectorDialogFragment {
            val languageSelectorDialogFragment = LanguageSelectorDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            languageSelectorDialogFragment.arguments = args

            return languageSelectorDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var listener: StateChangesListener? = null

    private var language = "en"

    private var lngEn: RadioButton? = null
    private var lngFr: RadioButton? = null
    private var lngDe: RadioButton? = null
    private var lngIt: RadioButton? = null
    private var lngJp: RadioButton? = null
    private var lngKp: RadioButton? = null
    private var lngCnS: RadioButton? = null
    private var lngCnT: RadioButton? = null
    private var lngEs: RadioButton? = null
    private var lngUk: RadioButton? = null
    private var lngRu: RadioButton? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_select_language, null)

        lngEn = view.findViewById(R.id.lngEn)
        lngFr = view.findViewById(R.id.lngFr)
        lngDe = view.findViewById(R.id.lngDe)
        lngIt = view.findViewById(R.id.lngIt)
        lngJp = view.findViewById(R.id.lngJp)
        lngKp = view.findViewById(R.id.lngKp)
        lngCnS = view.findViewById(R.id.lngCnS)
        lngCnT = view.findViewById(R.id.lngCnT)
        lngEs = view.findViewById(R.id.lngEs)
        lngUk = view.findViewById(R.id.lngUk)
        lngRu = view.findViewById(R.id.lngRu)

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ ->  }

        language = requireArguments().getString("name").toString()

        lngEn?.isChecked = language == "en"
        lngFr?.isChecked = language == "fr"
        lngDe?.isChecked = language == "de"
        lngIt?.isChecked = language == "it"
        lngJp?.isChecked = language == "ja"
        lngKp?.isChecked = language == "ko"
        lngCnS?.isChecked = language == "zh_CN"
        lngCnT?.isChecked = language == "zh_TW"
        lngEs?.isChecked = language == "es"
        lngUk?.isChecked = language == "uk"
        lngRu?.isChecked = language == "ru"

        lngEn?.setOnClickListener { language = "en" }
        lngFr?.setOnClickListener { language = "fr" }
        lngDe?.setOnClickListener { language = "de" }
        lngIt?.setOnClickListener { language = "it" }
        lngJp?.setOnClickListener { language = "ja" }
        lngKp?.setOnClickListener { language = "ko" }
        lngCnS?.setOnClickListener { language = "zh_CN" }
        lngCnT?.setOnClickListener { language = "zh_TW" }
        lngEs?.setOnClickListener { language = "es" }
        lngUk?.setOnClickListener { language = "uk" }
        lngRu?.setOnClickListener { language = "ru" }

        return builder!!.create()
    }

    private fun validateForm() {
        if (language != "") {
            listener!!.onSelected(language)
        } else {
            listener!!.onFormError(language)
        }
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onSelected(name: String)
        fun onFormError(name: String)
    }
}