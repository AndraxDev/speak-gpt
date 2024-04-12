package org.teslasoft.assistant.ui.activities

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.hardware.Camera

class CameraPreview(context: Context, private var mCamera: Camera?) : SurfaceView(context), SurfaceHolder.Callback {

    private var mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mCamera?.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Release the camera preview in activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, weight: Int, height: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    }
}
