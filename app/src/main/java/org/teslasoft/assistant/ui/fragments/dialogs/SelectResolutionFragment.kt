/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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

class SelectResolutionFragment : DialogFragment() {
    companion object {
        fun newInstance(resolution: String, chatId: String) : SelectResolutionFragment {
            val languageSelectorDialogFragment = SelectResolutionFragment()

            val args = Bundle()
            args.putString("resolution", resolution)
            args.putString("chatId", chatId)

            languageSelectorDialogFragment.arguments = args

            return languageSelectorDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var listener: StateChangesListener? = null

    private var r256: RadioButton? = null
    private var r512: RadioButton? = null
    private var r1024: RadioButton? = null

    private var resolution = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_resolution, null)

        r256 = view.findViewById(R.id.r256)
        r512 = view.findViewById(R.id.r512)
        r1024 = view.findViewById(R.id.r1024)

        resolution = requireArguments().getString("resolution").toString()

        r256?.isChecked = resolution == "256x256"
        r512?.isChecked = resolution == "512x512"
        r1024?.isChecked = resolution == "1024x1024"

        when (resolution) {
            "256x256" -> {
                clearSelection()
                r256?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                r256?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }

            "512x512" -> {
                clearSelection()
                r512?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                r512?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }

            "1024x1024" -> {
                clearSelection()
                r1024?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                r1024?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
        }

        r256?.setOnClickListener {
            clearSelection()
            r256?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            r256?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            resolution = "256x256"
        }

        r512?.setOnClickListener {
            clearSelection()
            r512?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            r512?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            resolution = "512x512"
        }

        r1024?.setOnClickListener {
            clearSelection()
            r1024?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            r1024?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            resolution = "1024x1024"
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> validateForm() }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

        return builder!!.create()
    }

    private fun clearSelection() {
        r256?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        r256?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        r512?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        r512?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        r1024?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        r1024?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
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
        if (resolution != "") {
            listener!!.onSelected(resolution)
        } else {
            listener!!.onFormError(resolution)
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
