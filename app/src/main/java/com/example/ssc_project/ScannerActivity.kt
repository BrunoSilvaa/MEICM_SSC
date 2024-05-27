package com.example.ssc_project

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

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

        findViewById<Button>(R.id.btnScanGraph).setOnClickListener {
            // Open the camera for scanning
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Handle the camera image
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Process the imageBitmap here
        }
    }
}