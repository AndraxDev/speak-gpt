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

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class AdvancedSettingsDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(name: String, chatId: String) : AdvancedSettingsDialogFragment {
            val advancedSettingsDialogFragment = AdvancedSettingsDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            advancedSettingsDialogFragment.arguments = args

            return advancedSettingsDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var gpt_35_turbo: RadioButton? = null
    private var gpt_35_turbo_0301: RadioButton? = null
    private var gpt_35_turbo_0613: RadioButton? = null
    private var gpt_4: RadioButton? = null
    private var gpt_4_0314: RadioButton? = null
    private var gpt_4_0613: RadioButton? = null
    private var gpt_4_32k: RadioButton? = null
    private var gpt_4_32k_0314: RadioButton? = null
    private var text_davinci_003: RadioButton? = null
    private var text_davinci_002: RadioButton? = null
    private var text_curie_001: RadioButton? = null
    private var text_babbage_001: RadioButton? = null
    private var text_ada_001: RadioButton? = null
    private var davinci: RadioButton? = null
    private var curie: RadioButton? = null
    private var babbage: RadioButton? = null
    private var ada: RadioButton? = null
    private var ft: RadioButton? = null
    private var ftInput: EditText? = null
    private var maxTokens: EditText? = null
    private var endSeparator: EditText? = null
    private var prefix: EditText? = null

    private var listener: StateChangesListener? = null

    private var model = "gpt-3.5-turbo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advanced_settings, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_advanced_settings, null)

        gpt_35_turbo = view.findViewById(R.id.gpt_35_turbo)
        gpt_35_turbo_0301 = view.findViewById(R.id.gpt_35_turbo_0301)
        gpt_35_turbo_0613 = view.findViewById(R.id.gpt_35_turbo_0613)
        gpt_4 = view.findViewById(R.id.gpt_4)
        gpt_4_0314 = view.findViewById(R.id.gpt_4_0314)
        gpt_4_0613 = view.findViewById(R.id.gpt_4_0613)
        gpt_4_32k = view.findViewById(R.id.gpt_4_32k)
        gpt_4_32k_0314 = view.findViewById(R.id.gpt_4_32k_0314)
        text_davinci_003 = view.findViewById(R.id.text_davinci_003)
        text_davinci_002 = view.findViewById(R.id.text_davinci_002)
        text_curie_001 = view.findViewById(R.id.text_curie_001)
        text_babbage_001 = view.findViewById(R.id.text_babbage_001)
        text_ada_001 = view.findViewById(R.id.text_ada_001)
        davinci = view.findViewById(R.id.davinci)
        curie = view.findViewById(R.id.curie)
        babbage = view.findViewById(R.id.babbage)
        ada = view.findViewById(R.id.ada)
        ft = view.findViewById(R.id.ft)
        ftInput = view.findViewById(R.id.ft_input)
        maxTokens = view.findViewById(R.id.max_tokens)
        endSeparator = view.findViewById(R.id.end_separator)
        prefix = view.findViewById(R.id.prefix)

        maxTokens?.setText(Preferences.getPreferences(requireActivity(), arguments?.getString("chatId")!!).getMaxTokens().toString())
        endSeparator?.setText(Preferences.getPreferences(requireActivity(), arguments?.getString("chatId")!!).getEndSeparator())
        prefix?.setText(Preferences.getPreferences(requireActivity(), arguments?.getString("chatId")!!).getPrefix())

        gpt_35_turbo?.setOnClickListener {
            model = "gpt-3.5-turbo"
            clearSelection()
            gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_35_turbo?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_35_turbo_0301?.setOnClickListener {
            model = "gpt-3.5-turbo-0301"
            clearSelection()
            gpt_35_turbo_0301?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_35_turbo_0301?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_35_turbo_0613?.setOnClickListener {
            model = "gpt-3.5-turbo-16k-0613"
            clearSelection()
            gpt_35_turbo_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_35_turbo_0613?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_4?.setOnClickListener {
            model = "gpt-4"
            clearSelection()
            gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_4_0314?.setOnClickListener {
            model = "gpt-4-0314"
            clearSelection()
            gpt_4_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_0314?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_4_0613?.setOnClickListener {
            model = "gpt-4-0613"
            clearSelection()
            gpt_4_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_0613?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_4_32k?.setOnClickListener {
            model = "gpt-4-32k"
            clearSelection()
            gpt_4_32k?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_32k?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        gpt_4_32k_0314?.setOnClickListener {
            model = "gpt-4-32k-0314"
            clearSelection()
            gpt_4_32k_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_32k_0314?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        text_davinci_003?.setOnClickListener {
            model = "text-davinci-003"
            clearSelection()
            text_davinci_003?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            text_davinci_003?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        text_davinci_002?.setOnClickListener {
            model = "text-davinci-002"
            clearSelection()
            text_davinci_002?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            text_davinci_002?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        text_curie_001?.setOnClickListener {
            model = "text-curie-001"
            clearSelection()
            text_curie_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            text_curie_001?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        text_babbage_001?.setOnClickListener {
            model = "text-babbage-001"
            clearSelection()
            text_babbage_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            text_babbage_001?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        text_ada_001?.setOnClickListener {
            model = "text-ada-001"
            clearSelection()
            text_ada_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            text_ada_001?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        davinci?.setOnClickListener {
            model = "davinci"
            clearSelection()
            davinci?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            davinci?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        curie?.setOnClickListener {
            model = "curie"
            clearSelection()
            curie?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            curie?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        babbage?.setOnClickListener {
            model = "babbage"
            clearSelection()
            babbage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            babbage?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        ada?.setOnClickListener {
            model = "ada"
            clearSelection()
            ada?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            ada?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.GONE
        }
        ft?.setOnClickListener {
            model = ftInput?.text.toString()
            clearSelection()
            ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            ft?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftInput?.visibility = View.VISIBLE
        }

        ftInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                model = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }
        })

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ ->  }

        model = requireArguments().getString("name").toString()

        when (requireArguments().getString("name")) { // load default model if settings not found
            "gpt-3.5-turbo" -> {
                gpt_35_turbo?.isChecked = true
                clearSelection()
                gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_35_turbo?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "gpt-3.5-turbo-16k-0613" -> {
                gpt_35_turbo_0613?.isChecked = true
                clearSelection()
                gpt_35_turbo_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_35_turbo_0613?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            /* DEPRECATED */
            /*"gpt-3.5-turbo-0301" -> {
                gpt_35_turbo_0301?.isChecked = true
                clearSelection()
                gpt_35_turbo_0301?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_35_turbo_0301?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }*/
            "gpt-4" -> {
                gpt_4?.isChecked = true
                clearSelection()
                gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "gpt-4-0613" -> {
                gpt_4_0613?.isChecked = true
                clearSelection()
                gpt_4_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_0613?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            /* DEPRECATED */
            /*"gpt-4-0314" -> {
                gpt_4_0314?.isChecked = true
                clearSelection()
                gpt_4_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_0314?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }*/
            "gpt-4-32k" -> {
                gpt_4_32k?.isChecked = true
                clearSelection()
                gpt_4_32k?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_32k?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            /* DEPRECATED */
            /*"gpt-4-32k-0314" -> {
                gpt_4_32k_0314?.isChecked = true
                clearSelection()
                gpt_4_32k_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_32k_0314?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }*/
            "text-davinci-003" -> {
                text_davinci_003?.isChecked = true
                clearSelection()
                text_davinci_003?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                text_davinci_003?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "text-davinci-002" -> {
                text_davinci_002?.isChecked = true
                clearSelection()
                text_davinci_002?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                text_davinci_002?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "text-curie-001" -> {
                text_curie_001?.isChecked = true
                clearSelection()
                text_curie_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                text_curie_001?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "text-babbage-001" -> {
                text_babbage_001?.isChecked = true
                clearSelection()
                text_babbage_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                text_babbage_001?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "text-ada-001" -> {
                text_ada_001?.isChecked = true
                clearSelection()
                text_ada_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                text_ada_001?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            /* DEPRECATED */
            /*"davinci" -> {
                davinci?.isChecked = true
                clearSelection()
                davinci?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                davinci?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "curie" -> {
                curie?.isChecked = true
                clearSelection()
                curie?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                curie?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "babbage" -> {
                babbage?.isChecked = true
                clearSelection()
                babbage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                babbage?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }
            "ada" -> {
                ada?.isChecked = true
                clearSelection()
                ada?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                ada?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.GONE
            }*/
            else -> {
                ft?.isChecked = true
                ftInput?.setText(requireArguments().getString("name"))
                clearSelection()
                ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                ft?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftInput?.visibility = View.VISIBLE
            }
        }

        return builder!!.create()
    }

    private fun clearSelection() {
        gpt_35_turbo?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_35_turbo_0301?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_35_turbo_0301?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_35_turbo_0613?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_35_turbo_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_0314?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_0613?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_0613?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_32k?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_32k?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_32k_0314?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_32k_0314?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        text_davinci_003?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        text_davinci_003?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        text_davinci_002?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        text_davinci_002?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        text_curie_001?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        text_curie_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        text_babbage_001?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        text_babbage_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        text_ada_001?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        text_ada_001?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        davinci?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        davinci?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        curie?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        curie?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        babbage?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        babbage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        ada?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        ada?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        ft?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2())
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_3.getColor(context)
    }

    private fun getSurfaceColorV2() : Int {
        return requireActivity().getColor(R.color.accent_900)
    }

    private fun validateForm() {
        if (ftInput?.text.toString() == "" && ft?.isChecked == true) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString() == "") {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 8192 && model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 2048 && !model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        listener!!.onSelected(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onSelected(name: String, maxTokens: String, endSeparator: String, prefix: String)
        fun onFormError(name: String, maxTokens: String, endSeparator: String, prefix: String)
    }
}
