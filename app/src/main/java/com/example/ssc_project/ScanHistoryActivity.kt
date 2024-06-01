package com.example.ssc_project

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class ScanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScanHistoryAdapter
    private lateinit var scanList: MutableList<ScanItem>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load saved images
        loadSavedImages()

        adapter = ScanHistoryAdapter(this, scanList)
        recyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            refreshScanHistory()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        NavigationUtil.setupBottomNavigation(this, bottomNavigationView)
    }

    private fun loadSavedImages() {
        val albumName = "Graph_Analyser"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            albumName
        )

        scanList = mutableListOf()
        if (storageDir.exists()) {
            val files = storageDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.extension == "jpg") {
                        val title = file.nameWithoutExtension
                        val description = "This is a scan saved on ${file.lastModified()}"
                        scanList.add(ScanItem(title, description, file.absolutePath))
                    }
                }
            }
        }
    }

    private fun refreshScanHistory() {
        loadSavedImages() // Reload the saved images
        adapter.notifyDataSetChanged() // Notify the adapter about the data change
        swipeRefreshLayout.isRefreshing = false // Stop the refreshing animation
    }
}
