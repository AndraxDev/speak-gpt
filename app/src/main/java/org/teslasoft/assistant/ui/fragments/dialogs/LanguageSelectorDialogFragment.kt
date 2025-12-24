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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
    private var lngPl: RadioButton? = null
    private var lngTr: RadioButton? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

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
        lngPl = view.findViewById(R.id.lngPl)
        lngTr = view.findViewById(R.id.lngTr)

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> validateForm() }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

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
        lngPl?.isChecked = language == "pl"
        lngTr?.isChecked = language == "tr"

        when (language) {
            "en" -> {
                clearSelection()
                lngEn?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngEn?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "fr" -> {
                clearSelection()
                lngFr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngFr?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "de" -> {
                clearSelection()
                lngDe?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngDe?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "it" -> {
                clearSelection()
                lngIt?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngIt?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "ja" -> {
                clearSelection()
                lngJp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngJp?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "ko" -> {
                clearSelection()
                lngKp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngKp?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "zh_CN" -> {
                clearSelection()
                lngCnS?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngCnS?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "zh_TW" -> {
                clearSelection()
                lngCnT?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngCnT?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "es" -> {
                clearSelection()
                lngEs?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngEs?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "uk" -> {
                clearSelection()
                lngUk?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngUk?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "ru" -> {
                clearSelection()
                lngRu?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngRu?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "pl" -> {
                clearSelection()
                lngPl?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngPl?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
            "tr" -> {
                clearSelection()
                lngTr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                lngTr?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
        }

        lngEn?.setOnClickListener {
            language = "en"
            clearSelection()
            lngEn?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngEn?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngFr?.setOnClickListener {
            language = "fr"
            clearSelection()
            lngFr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngFr?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngDe?.setOnClickListener {
            language = "de"
            clearSelection()
            lngDe?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngDe?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngIt?.setOnClickListener {
            language = "it"
            clearSelection()
            lngIt?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngIt?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngJp?.setOnClickListener {
            language = "ja"
            clearSelection()
            lngJp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngJp?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngKp?.setOnClickListener {
            language = "ko"
            clearSelection()
            lngKp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngKp?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngCnS?.setOnClickListener {
            language = "zh_CN"
            clearSelection()
            lngCnS?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngCnS?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngCnT?.setOnClickListener {
            language = "zh_TW"
            clearSelection()
            lngCnT?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngCnT?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngEs?.setOnClickListener {
            language = "es"
            clearSelection()
            lngEs?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngEs?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngUk?.setOnClickListener {
            language = "uk"
            clearSelection()
            lngUk?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngUk?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngRu?.setOnClickListener {
            language = "ru"
            clearSelection()
            lngRu?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngRu?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngPl?.setOnClickListener {
            language = "pl"
            clearSelection()
            lngPl?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngPl?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }
        lngTr?.setOnClickListener {
            language = "tr"
            clearSelection()
            lngTr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            lngTr?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        }

        return builder!!.create()
    }

    private fun clearSelection() {
        lngEn?.background = getDarkAccentDrawable(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngEn?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngFr?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngFr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngDe?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngDe?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngIt?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngIt?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngJp?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngJp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngKp?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngKp?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngCnS?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngCnS?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngCnT?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngCnT?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngEs?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngEs?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngUk?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngUk?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngRu?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngRu?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngPl?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngPl?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        lngTr?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        lngTr?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return context.getColor(android.R.color.transparent)
    }

    private fun getSurfaceColorV2(context: Context) : Int {
        return context.getColor(R.color.accent_900)
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
