package com.example.enso.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.example.enso.customer.activities.MapViewActivity
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the search bar layout and set a click listener to open SearchFragment
        val llSearchBar = view.findViewById<LinearLayout>(R.id.llSearchBar)
        llSearchBar.setOnClickListener {
            (activity as? MainActivity)?.switchToSearch()
        }

        // View on Map click listener
        val tvViewOnMap = view.findViewById<TextView>(R.id.tvViewOnMap)
        tvViewOnMap.setOnClickListener {
            val intent = Intent(requireContext(), MapViewActivity::class.java)
            startContext(intent)
        }

        // Salon Cards click listeners
        val cvHairAvenue = view.findViewById<MaterialCardView>(R.id.cvHairAvenue)
        val cvCentralSalon = view.findViewById<MaterialCardView>(R.id.cvCentralSalon)

        cvHairAvenue?.setOnClickListener {
            (activity as? MainActivity)?.switchToSalonDetails()
        }

        cvCentralSalon?.setOnClickListener {
            (activity as? MainActivity)?.switchToSalonDetails()
        }
    }

    private fun startContext(intent: Intent) {
        startActivity(intent)
    }
}