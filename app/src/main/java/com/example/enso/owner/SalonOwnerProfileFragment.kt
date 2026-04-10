package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.admin.AddSalonActivity

class SalonOwnerProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_salon_owner_profile, container, false)

        // Identify the "Salon Details" row
        // Since we are not modifying the UI XML to add an ID, 
        // we find the row by its position: cvSalonSection -> LinearLayout -> First Child
        val cvSalonSection = view.findViewById<ViewGroup>(R.id.cvSalonSection)
        val salonSectionContainer = cvSalonSection.getChildAt(0) as ViewGroup
        val salonDetailsRow = salonSectionContainer.getChildAt(0)

        // Set click listener to open AddSalonActivity
        salonDetailsRow.setOnClickListener {
            val intent = Intent(requireContext(), AddSalonActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
