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

class SelectImageModelFragment : DialogFragment() {
    companion object {
        fun newInstance(imageModel: String, chatId: String) : SelectImageModelFragment {
            val languageSelectorDialogFragment = SelectImageModelFragment()

            val args = Bundle()
            args.putString("imageModel", imageModel)
            args.putString("chatId", chatId)

            languageSelectorDialogFragment.arguments = args

            return languageSelectorDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var listener: StateChangesListener? = null

    private var dalle2: RadioButton? = null
    private var dalle3: RadioButton? = null
    private var gptImage: RadioButton? = null

    private var imageModel = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_image_model, null)

        dalle2 = view.findViewById(R.id.dalle2)
        dalle3 = view.findViewById(R.id.dalle3)
        gptImage = view.findViewById(R.id.gpt_image)

        imageModel = requireArguments().getString("imageModel").toString()

        dalle2?.isChecked = imageModel == "dall-e-2"
        dalle3?.isChecked = imageModel == "dall-e-3"
        gptImage?.isChecked = imageModel == "gpt-image-1"

        when (imageModel) {
            "dall-e-2" -> {
                clearSelection()
                dalle2?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                dalle2?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }

            "dall-e-3" -> {
                clearSelection()
                dalle3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                dalle3?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }

            "gpt-image-1" -> {
                clearSelection()
                gptImage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gptImage?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            }
        }

        dalle2?.setOnClickListener {
            clearSelection()
            dalle2?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            dalle2?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            imageModel = "dall-e-2"
        }

        dalle3?.setOnClickListener {
            clearSelection()
            dalle3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            dalle3?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            imageModel = "dall-e-3"
        }

        gptImage?.setOnClickListener {
            clearSelection()
            gptImage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gptImage?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
            imageModel = "gpt-image-1"
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> validateForm() }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

        return builder!!.create()
    }

    private fun clearSelection() {
        dalle2?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        dalle2?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        dalle3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        dalle3?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!, requireActivity())
        gptImage?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.accent_900))
        gptImage?.background = getDarkAccentDrawable(
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
        if (imageModel != "") {
            listener!!.onSelected(imageModel)
        } else {
            listener!!.onFormError(imageModel)
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
