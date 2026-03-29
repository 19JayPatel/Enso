package com.example.enso.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.auth.LoginActivity
import com.example.enso.databinding.FragmentAdminSalonsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminSalonsFragment : Fragment() {

    private var _binding: FragmentAdminSalonsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentAdminSalonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 🔥 ADMIN SESSION CHECK
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Redirect to Log in if session is null
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        // 2. 🔥 FETCH ONLY OWNERS FROM FIREBASE REALTIME DATABASE
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val ownerList = mutableListOf<Map<String, Any>>()

                // Loop through all users and pick only those with role "owner"
                for (userSnap in snapshot.children) {
                    val role = userSnap.child("role").value.toString()
                    if (role == "owner") {
                        val data = mutableMapOf<String, Any>()
                        data["name"] = userSnap.child("name").value.toString()
                        data["salonName"] = userSnap.child("salonName").value.toString()
                        data["status"] = userSnap.child("status").value.toString()
                        ownerList.add(data)
                    }
                }

                // 3. 🔥 MAKE DATA DYNAMIC (Map data to the 5 static cards in your XML)
                if (ownerList.size >= 1) updateCard(ownerList[0], binding.tvSalonNameHa, binding.tvInitialsHa, binding.tvOwnerHa, binding.tvStatusHa)
                if (ownerList.size >= 2) updateCard(ownerList[1], binding.tvSalonNameCs, binding.tvInitialsCs, binding.tvOwnerCs, binding.tvStatusCs)
                if (ownerList.size >= 3) updateCard(ownerList[2], binding.tvSalonNameGs, binding.tvInitialsGs, binding.tvOwnerGs, binding.tvStatusGs)
                if (ownerList.size >= 4) updateCard(ownerList[3], binding.tvSalonNamePl, binding.tvInitialsPl, binding.tvOwnerPl, binding.tvStatusPl)
                if (ownerList.size >= 5) updateCard(ownerList[4], binding.tvSalonNameVb, binding.tvInitialsVb, binding.tvOwnerVb, binding.tvStatusVb)
            }
        }

        // 4. 🔥 CONNECT "ADD NEW SALON" BUTTON
        binding.btnAddSalon.setOnClickListener {
            val intent = Intent(requireContext(), AddSalonActivity::class.java)
            startActivity(intent)
        }
    }

    // Helper function to update UI for a specific card
    private fun updateCard(ownerData: Map<String, Any>, tvSalon: TextView, tvInitials: TextView, tvOwner: TextView, tvStatus: TextView) {
        val salonName = ownerData["salonName"].toString()
        val ownerName = ownerData["name"].toString()
        val status = ownerData["status"].toString()

        // Set Dynamic Salon and Owner names
        tvSalon.text = salonName
        tvOwner.text = "Owner: $ownerName"
        tvStatus.text = status

        // 5. 🔥 GENERATE INITIALS (Logic: HA, CS...)
        try {
            val words = salonName.trim().split(" ")
            if (words.size >= 2) {
                val initials = words[0][0].toString() + words[1][0].toString()
                tvInitials.text = initials.uppercase()
            } else if (salonName.isNotEmpty()) {
                tvInitials.text = salonName.take(2).uppercase()
            }
        } catch (e: Exception) {
            tvInitials.text = "S"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
