package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.enso.customer.fragments.CalendarFragment
import com.example.enso.customer.fragments.HeartFragment
import com.example.enso.customer.fragments.HomeFragment
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.example.enso.customer.fragments.ProfileFragment
import com.example.enso.customer.fragments.SalonDetailsFragment
import com.example.enso.customer.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView

    companion object {
        const val NAV_HOME = 1
        const val NAV_CALENDAR = 2
        const val NAV_HEART = 3
        const val NAV_PROFILE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 SESSION CHECK: If user is not logged in, go to LoginActivity
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return // Stop further execution
        }

        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)

        // Remove active indicator programmatically for older versions
        bottomNav.itemActiveIndicatorColor = null

        // Add menu items manually
        bottomNav.menu.add(0, NAV_HOME, 0, "").setIcon(R.drawable.ic_home_selector)
        bottomNav.menu.add(0, NAV_CALENDAR, 1, "").setIcon(R.drawable.ic_calendar_selector)
        bottomNav.menu.add(0, NAV_HEART, 2, "").setIcon(R.drawable.ic_heart_selector)
        bottomNav.menu.add(0, NAV_PROFILE, 3, "").setIcon(R.drawable.ic_profile_selector)

        // Set Home as selected
        bottomNav.selectedItemId = NAV_HOME
        loadFragment(HomeFragment())

        // Click handling
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                NAV_HOME -> {
                    loadFragment(HomeFragment())
                    true
                }

                NAV_CALENDAR -> {
                    loadFragment(CalendarFragment())
                    true
                }

                NAV_HEART -> {
                    loadFragment(HeartFragment())
                    true
                }

                NAV_PROFILE -> {
                    loadFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun switchToSearch() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, SearchFragment())
            .addToBackStack(null)
            .commit()
    }

    fun switchToSalonDetails() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, SalonDetailsFragment())
            .addToBackStack(null)
            .commit()
    }

}
