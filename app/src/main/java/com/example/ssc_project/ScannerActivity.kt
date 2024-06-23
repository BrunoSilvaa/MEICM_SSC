package com.example.ssc_project

import BboxResponse
import LocalApiService
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ScannerActivity : AppCompatActivity() {

    private var cameraPreview: CameraPreview? = null
    private var mCamera: Camera? = null
    private val REQUEST_IMAGE_PICK = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        NavigationUtil.setupBottomNavigation(this, bottomNavigationView)
        val preview: FrameLayout = findViewById(R.id.camera_preview)
        val btnScanGraph: Button = findViewById(R.id.btnScanGraph)

        // Initialize the select image button
        val selectImageButton: ImageButton = findViewById(R.id.button_select_image)
        selectImageButton.setOnClickListener {
            openImagePicker()
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
        } else { // back-facing
            (info.orientation - degrees + 360) % 360
        }

        mCamera?.setDisplayOrientation(result)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val pictureCallback = Camera.PictureCallback { data, _ ->
        // Handle the captured image data
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        // Rotate the bitmap to correct orientation
        val rotatedBitmap = rotateBitmap(bitmap, 90f)

        saveImageToExternalStorage(rotatedBitmap)

        classifyImageWithLocalApi(rotatedBitmap)
        // Navigate to ScanHistoryActivity
        val intent = Intent(this, ScanHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            //classifyImageWithLocalApi(bitmap)
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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                val bitmap = getBitmapFromUri(selectedImageUri)
                if (bitmap != null) {
                    saveAnnotatedImageToExternalStorage(bitmap)
                    // Navigate to ScanHistoryActivity
                    val intent = Intent(this, ScanHistoryActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    private fun drawBoundingBoxes(bitmap: Bitmap, bboxList: List<List<Float>>): Bitmap {
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        for (bbox in bboxList) {
            val left = bbox[0] * resultBitmap.width
            val top = bbox[1] * resultBitmap.height
            val right = left + (bbox[2] * resultBitmap.width)
            val bottom = top + (bbox[3] * resultBitmap.height)
            canvas.drawRect(left, top, right, bottom, paint)
        }

        return resultBitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun classifyImageWithLocalApi(bitmap: Bitmap) {
        val apiService = RetrofitClient.instance.create(LocalApiService::class.java)
        val imageBase64 = convertImageToBase64(bitmap)

        val call = apiService.classifyImage(imageBase64)
        call.enqueue(object : Callback<BboxResponse> {
            override fun onResponse(call: Call<BboxResponse>, response: Response<BboxResponse>) {
                if (response.isSuccessful) {
                    val bboxResponse = response.body()
                    bboxResponse?.bbox_predictions?.let {
                        Log.d("BboxResponse", it.toString())
                        val annotatedBitmap = drawBoundingBoxes(bitmap, it)
                        saveAnnotatedImageToExternalStorage(annotatedBitmap)
                    }
                } else {
                    Log.e("BboxResponse", "Response not successful")
                }
            }

            override fun onFailure(call: Call<BboxResponse>, t: Throwable) {
                Log.e("BboxResponse", "Request failed", t)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveAnnotatedImageToExternalStorage(bitmap: Bitmap) {
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
        val imageFileName = "ANNOTATED_IMG_$timeStamp.jpg"
        val imageFile = File(storageDir, imageFileName)

        try {
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }
            Toast.makeText(this, "Annotated image saved: ${imageFile.absolutePath}", Toast.LENGTH_LONG).show()
            //classifyImageWithLocalApi(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save annotated image", Toast.LENGTH_SHORT).show()
        }
    }

}
