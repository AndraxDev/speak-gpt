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
import com.google.android.material.elevation.SurfaceColors
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
                setSelection(dalle2, null)
            }

            "dall-e-3" -> {
                setSelection(dalle3, null)
            }

            "gpt-image-1" -> {
                setSelection(gptImage, null)
            }
        }

        bindOnClickListener(dalle2, "dall-e-2")
        bindOnClickListener(dalle3, "dall-e-3")
        bindOnClickListener(gptImage, "gpt-image-1")

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> validateForm() }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

        return builder!!.create()
    }

    private fun bindOnClickListener(view: RadioButton?, value: String) {
        view?.setOnClickListener {
            setSelection(view, value)
        }
    }

    private fun setSelection(view: RadioButton?, value: String?) {
        clearSelection()
        view?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
        view?.background = getDarkAccentDrawableV2(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent)!!, requireActivity())
        if (value != null) imageModel = value
    }

    private fun clearSingleSelection(view: RadioButton?, isTop: Boolean = false, isBottom: Boolean = false) {
        var background = R.drawable.btn_accent_center
        if (isTop) background = R.drawable.btn_accent_top
        if (isBottom) background = R.drawable.btn_accent_bottom
        view?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), background)!!, requireActivity())
        view?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))
    }

    private fun clearSelection() {
        clearSingleSelection(dalle2, isTop = true)
        clearSingleSelection(dalle3)
        clearSingleSelection(gptImage, isBottom = true)
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
        return SurfaceColors.SURFACE_5.getColor(context)
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
