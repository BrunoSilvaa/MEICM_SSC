package com.example.ssc_project

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream

class ScannerActivity : AppCompatActivity() {

    private var cameraPreview: CameraPreview? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val preview: FrameLayout = findViewById(R.id.camera_preview)
        val btnScanGraph: Button = findViewById(R.id.btnScanGraph)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_history -> {
                    // Handle history navigation
                    true
                }
                R.id.navigation_scan -> {
                    // Current screen, do nothing
                    true
                }
                R.id.navigation_settings -> {
                    // Handle settings navigation
                    true
                }
                else -> false
            }
        }

        btnScanGraph.setOnClickListener {
            // Capture the image
            mCamera?.takePicture(null, null, pictureCallback)
        }

        // Check permissions and initialize the camera preview
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            initializeCameraPreview(preview)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun initializeCameraPreview(preview: FrameLayout) {
        try {
            mCamera = Camera.open()
            cameraPreview = CameraPreview(this, mCamera)
            preview.addView(cameraPreview)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to camera service", Toast.LENGTH_SHORT).show()
        }
    }

    private val pictureCallback = Camera.PictureCallback { data, _ ->
        // Handle the captured image data
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        // You can process the bitmap here, like displaying it or saving it
        // For now, let's display a toast
        Toast.makeText(this, "Picture taken!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, initialize the camera preview
                val preview: FrameLayout = findViewById(R.id.camera_preview)
                initializeCameraPreview(preview)
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission is required to scan graphs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 2
    }
}
