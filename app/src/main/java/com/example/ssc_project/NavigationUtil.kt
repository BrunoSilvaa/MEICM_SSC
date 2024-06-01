package com.example.ssc_project

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtil {

    fun setupBottomNavigation(activity: Activity, bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_history -> {
                    if (activity !is ScanHistoryActivity) {
                        activity.startActivity(Intent(activity, ScanHistoryActivity::class.java))
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_scan -> {
                    if (activity !is ScannerActivity) {
                        activity.startActivity(Intent(activity, ScannerActivity::class.java))
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }

        // Set the current selected item
        when (activity) {
            is ScanHistoryActivity -> bottomNavigationView.selectedItemId = R.id.navigation_history
            is ScannerActivity -> bottomNavigationView.selectedItemId = R.id.navigation_scan
        }
    }
}
