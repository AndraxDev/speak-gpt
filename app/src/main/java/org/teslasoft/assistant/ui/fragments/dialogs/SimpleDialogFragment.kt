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
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.MaterialContainerTransform
import org.teslasoft.assistant.R
import androidx.core.graphics.drawable.toDrawable

class SimpleDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Dialog Title")
            .setMessage("Dialog Message")
            .setPositiveButton(R.string.btn_close) { _, _ -> }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable shared element transitions
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()
    }

    private fun buildContainerTransform(): MaterialContainerTransform {
        return MaterialContainerTransform().apply {
            drawingViewId = R.id.tile_bg  // The ID of the root view in your activity or fragment
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(SurfaceColors.SURFACE_3.getColor(requireContext()))
        }
    }

    override fun onStart() {
        super.onStart()
        // Adjust the dialog window to match the material container transform expectations
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            // Set the shared element transition name on the dialog's decor view
            decorView.transitionName = "function_tile"
        }
    }
}
