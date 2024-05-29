package com.example.ssc_project

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // Capture the image with auto-focus
            mCamera?.autoFocus { _, _ ->
                mCamera?.takePicture(null, null, pictureCallback)
            }
        }

        // Check permissions and initialize the camera preview
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            initializeCameraPreview()
        }
    }

    private fun initializeCameraPreview() {
        try {
            mCamera = Camera.open()
            cameraPreview = CameraPreview(this, mCamera)
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(cameraPreview)

            // Set camera orientation
            setCameraDisplayOrientation()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to camera service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCameraDisplayOrientation() {
        val rotation = windowManager.defaultDisplay.rotation
        val degrees = when (rotation) {
            0 -> 0
            1 -> 90
            2 -> 180
            3 -> 270
            else -> 0
        }

        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)

        val result = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (info.orientation + degrees) % 360
            //(360 - result) % 360 // compensate the mirror
        } else { // back-facing
            (info.orientation - degrees + 360) % 360
        }

        mCamera?.setDisplayOrientation(result)
    }



    private val pictureCallback = Camera.PictureCallback { data, _ ->
        // Handle the captured image data
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        // Rotate the bitmap to correct orientation
        val rotatedBitmap = rotateBitmap(bitmap, 90f)

        saveImageToExternalStorage(rotatedBitmap)
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap) {
        val albumName = "Graph_Analyser"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            albumName
        )

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val imageFile = File(storageDir, imageFileName)

        try {
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }
            Toast.makeText(this, "Image saved: ${imageFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permission granted, initialize the camera preview
                initializeCameraPreview()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission is required to scan and save graphs", Toast.LENGTH_SHORT).show()
                // Optionally, you can guide the user to the app settings to manually enable permissions
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }
}
