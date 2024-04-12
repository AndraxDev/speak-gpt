package org.teslasoft.assistant.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.permission.CameraPermissionActivity

class CameraActivity : FragmentActivity() {
    private lateinit var mPreview: CameraPreview

    private var btnShot: LinearLayout? = null

    private var cameraOutput: SurfaceView? = null

    private var camera: Camera? = null

    override fun onPause() {
        super.onPause()
        // Release the camera immediately on pause event.
        camera?.release()
        camera = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        btnShot = findViewById(R.id.btn_shot)

        cameraOutput = findViewById(R.id.camera_output)

        camera = Camera.open()
        camera?.setDisplayOrientation(90)
        mPreview = CameraPreview(this, camera).also {
            findViewById<SurfaceView>(R.id.camera_output).holder.addCallback(it)
        }

        btnShot?.setOnClickListener {
            takeScreenshotOfView(cameraOutput!!, cameraOutput!!.width, cameraOutput!!.height).let { bitmap ->
                val intent = Intent()
                intent.putExtra("data", bitmap)
                setResult(Activity.RESULT_OK, intent)
                // finish()
            }
        }
    }

    private fun takeScreenshotOfView(view: View, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        data?.let {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            // Here you can add extra code to manipulate the bitmap
            // For example, return the bitmap via intent
            val intent = Intent()
            intent.putExtra("data", bitmap)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } ?: Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
    }
}