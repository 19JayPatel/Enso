package com.example.enso.owner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main Activity for Salon Owner
 * Handles fragment switching using BottomNavigationView
 */
class SalonOwnerMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salonowner_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 1. Set Default Fragment (Dashboard) when activity starts
        if (savedInstanceState == null) {
            loadFragment(SalonOwnerDashboardFragment())
        }

        // 2. Set listener for menu item clicks
        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> SalonOwnerDashboardFragment()
                R.id.nav_services -> SalonOwnerServicesFragment()
                R.id.nav_bookings -> SalonOwnerBookingsFragment()
                R.id.nav_profile -> SalonOwnerProfileFragment()
                else -> SalonOwnerDashboardFragment()
            }
            
            // Switch to the selected fragment
            loadFragment(selectedFragment)
            true
        }
    }

    /**
     * Helper function to replace fragments in the container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
