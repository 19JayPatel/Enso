package com.example.enso.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.auth.LoginActivity
import com.example.enso.databinding.FragmentUsersDashboardBinding
import com.example.enso.databinding.ItemUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class UsersDashboardFragment : Fragment() {

    private var _binding: FragmentUsersDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 1. ADMIN SESSION CHECK
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        // 🔥 2. FETCH USERS FROM FIREBASE
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                binding.llUserList.removeAllViews() // Clear static content

                var total = 0
                var active = 0
                var inactive = 0
                var banned = 0

                for (userSnap in snapshot.children) {
                    val role = userSnap.child("role").value.toString()
                    Log.d("USER_DEBUG", "Role: $role")
                    
                    // ✅ Show ALL users (Force show both types)
                    if (role == "customer" || role == "salon_owner" || role == "owner") {
                        val name = userSnap.child("name").value.toString()
                        val email = userSnap.child("email").value.toString()
                        val status = userSnap.child("status").value.toString()
                        val createdAt = userSnap.child("createdAt").value as? Long ?: 0L

                        // Inflate item layout and add to container
                        val itemBinding = ItemUserBinding.inflate(layoutInflater, binding.llUserList, false)
                        
                        // Set data
                        val displayRole = if (role == "salon_owner" || role == "owner") "Salon Owner" else "Customer"
                        itemBinding.tvName.text = "$name ($displayRole)"
                        itemBinding.tvEmail.text = email
                        itemBinding.tvStatus.text = status
                        
                        // Initials logic
                        try {
                            val words = name.trim().split(" ")
                            if (words.size >= 2) {
                                itemBinding.tvInitials.text = (words[0][0].toString() + words[1][0].toString()).uppercase()
                            } else {
                                itemBinding.tvInitials.text = name.take(1).uppercase()
                            }
                        } catch (e: Exception) { itemBinding.tvInitials.text = "U" }

                        // Date logic
                        if (createdAt > 0) {
                            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                            itemBinding.tvJoinedDate.text = "Joined ${sdf.format(Date(createdAt))}"
                        } else {
                            itemBinding.tvJoinedDate.text = "Joined recently"
                        }

                        itemBinding.tvBookings.text = "0 bookings"

                        binding.llUserList.addView(itemBinding.root)

                        // Update summary counters
                        total++
                        when (status.lowercase()) {
                            "active" -> active++
                            "inactive" -> inactive++
                            "banned" -> banned++
                        }
                    }
                }

                // Update summary UI
                binding.tvTotalMembers.text = "$total members"
                binding.tvSummaryTotal.text = total.toString()
                binding.tvSummaryActive.text = active.toString()
                binding.tvSummaryInactive.text = inactive.toString()
                binding.tvSummaryBanned.text = banned.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
