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
                setSelection(r256, null)
            }

            "512x512" -> {
                setSelection(r512, null)
            }

            "1024x1024" -> {
                setSelection(r1024, null)
            }
        }

        bindOnClickListener(r256, "256x256")
        bindOnClickListener(r512, "512x512")
        bindOnClickListener(r1024, "1024x1024")

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
        if (value != null) resolution = value
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
        clearSingleSelection(r256, isTop = true)
        clearSingleSelection(r512)
        clearSingleSelection(r1024, isBottom = true)
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
