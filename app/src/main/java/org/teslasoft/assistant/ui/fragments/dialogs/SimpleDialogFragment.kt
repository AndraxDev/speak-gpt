package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.MaterialContainerTransform
import org.teslasoft.assistant.R

class SimpleDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create the standard Material Alert Dialog
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

    // Extension function to get color from theme
    private fun Context.themeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    override fun onStart() {
        super.onStart()
        // Adjust the dialog window to match the material container transform expectations
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Set the shared element transition name on the dialog's decor view
            decorView.transitionName = "function_tile"
        }
    }
}