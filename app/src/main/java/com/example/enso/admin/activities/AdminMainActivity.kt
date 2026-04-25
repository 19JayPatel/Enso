package com.example.enso.admin.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.admin.fragments.AdminDashboardFragment
import com.example.enso.admin.fragments.AdminProfileFragment
import com.example.enso.admin.fragments.AdminSalonsFragment
import com.example.enso.admin.fragments.UsersDashboardFragment
import com.example.enso.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment
        loadFragment(AdminDashboardFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(AdminDashboardFragment())
                R.id.nav_salons -> loadFragment(AdminSalonsFragment())
                R.id.nav_users -> loadFragment(UsersDashboardFragment())
                R.id.nav_profile -> loadFragment(AdminProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

class PlaceholderFragment(private val title: String) : Fragment() {
    // Basic placeholder fragment for non-implemented tabs
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return view
    }
}
