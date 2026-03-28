package com.example.enso.customer.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.google.android.material.card.MaterialCardView

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button to go to Home
        val btnBack = view.findViewById<MaterialCardView>(R.id.btnBack)
        btnBack?.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.bottomNav?.selectedItemId = MainActivity.NAV_HOME
        }

        // if user clicks on edit profile section then redirects to edit profile fragment
        val editProfileSection = view.findViewById<LinearLayout>(R.id.item_edit_profile)
        editProfileSection?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}