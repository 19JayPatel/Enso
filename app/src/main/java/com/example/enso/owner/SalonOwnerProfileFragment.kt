package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.admin.AddSalonActivity
import com.example.enso.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SalonOwnerProfileFragment : Fragment() {

    private lateinit var tvOwnerName: TextView
    private lateinit var tvOwnerSubtitle: TextView
    private lateinit var tvOwnerEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_profile, container, false)

        // ✅ STEP 1: GET CURRENT USER
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return view
        }

        val userId = user.uid

        // Initialize UI
        tvOwnerName = view.findViewById(R.id.tvOwnerName)
        tvOwnerSubtitle = view.findViewById(R.id.tvOwnerSubtitle)
        tvOwnerEmail = view.findViewById(R.id.tvOwnerEmail)

        // ✅ STEP 2: PROFILE (OWNER NAME & EMAIL DYNAMIC)
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    tvOwnerName.text = name ?: "Owner Name"
                    tvOwnerEmail.text = email ?: "No Email"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // ✅ STEP 3: SALON NAME DYNAMIC (Subtitle)
        val salonRef = FirebaseDatabase.getInstance().getReference("Salons")
        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (salonSnap in snapshot.children) {
                        val ownerId = salonSnap.child("ownerId").getValue(String::class.java)
                        if (ownerId == userId) {
                            val salonName = salonSnap.child("salonName").getValue(String::class.java)
                            tvOwnerSubtitle.text = "${salonName ?: "Salon"} • Owner"
                            break
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Original logic: Set click listener to open AddSalonActivity
        val cvSalonSection = view.findViewById<ViewGroup>(R.id.cvSalonSection)
        if (cvSalonSection != null) {
            val salonSectionContainer = cvSalonSection.getChildAt(0) as ViewGroup
            val salonDetailsRow = salonSectionContainer.getChildAt(0)
            salonDetailsRow.setOnClickListener {
                val intent = Intent(requireContext(), AddSalonActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }
}
