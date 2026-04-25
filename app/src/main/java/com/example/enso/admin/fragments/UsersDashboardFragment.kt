package com.example.enso.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.enso.auth.LoginActivity
import com.example.enso.databinding.FragmentUsersDashboardBinding
import com.example.enso.databinding.ItemUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        fetchUsersAndBookingCounts()
    }

    /**
     * FIX: Fetch all users and all bookings to calculate dynamic counts.
     */
    private fun fetchUsersAndBookingCounts() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings")

        usersRef.get().addOnSuccessListener { userSnapshot ->
            if (!userSnapshot.exists()) return@addOnSuccessListener

            bookingsRef.get().addOnSuccessListener { bookingSnapshot ->

                // STEP 1: Build Count Maps for both Customers and Owners
                val customerBookingMap = HashMap<String, Int>()
                val ownerBookingMap = HashMap<String, Int>()

                for (bookingSnap in bookingSnapshot.children) {
                    val customerId = bookingSnap.child("customerId").getValue(String::class.java)
                    val ownerId = bookingSnap.child("ownerId").getValue(String::class.java)

                    if (!customerId.isNullOrEmpty()) {
                        customerBookingMap[customerId] = (customerBookingMap[customerId] ?: 0) + 1
                    }
                    if (!ownerId.isNullOrEmpty()) {
                        ownerBookingMap[ownerId] = (ownerBookingMap[ownerId] ?: 0) + 1
                    }
                }

                // STEP 2: Render UI
                if (_binding == null) return@addOnSuccessListener
                binding.llUserList.removeAllViews()

                var totalMembers = 0
                var activeCount = 0
                var inactiveCount = 0
                var bannedCount = 0

                for (userSnap in userSnapshot.children) {
                    val role = userSnap.child("role").getValue(String::class.java) ?: ""
                    val userId = userSnap.key ?: ""

                    // Only show Customers and Salon Owners
                    if (role == "customer" || role == "salon_owner" || role == "owner") {
                        val name = userSnap.child("name").getValue(String::class.java) ?: "Unknown"
                        val email = userSnap.child("email").getValue(String::class.java) ?: ""
                        val status = userSnap.child("status").getValue(String::class.java) ?: "active"
                        val createdAt = userSnap.child("createdAt").getValue(Long::class.java) ?: 0L

                        // Determine booking count based on role
                        val bookingsCount = when (role) {
                            "customer" -> customerBookingMap[userId] ?: 0
                            "salon_owner", "owner" -> ownerBookingMap[userId] ?: 0
                            else -> 0
                        }

                        // Inflate and Bind
                        val itemBinding = ItemUserBinding.inflate(layoutInflater, binding.llUserList, false)

                        val displayRole = if (role == "customer") "Customer" else "Salon Owner"
                        itemBinding.tvName.text = "$name ($displayRole)"
                        itemBinding.tvEmail.text = email
                        itemBinding.tvStatus.text = status.replaceFirstChar { it.uppercase() }
                        itemBinding.tvBookings.text = "$bookingsCount Bookings"

                        // Initials logic
                        try {
                            val words = name.trim().split(" ")
                            itemBinding.tvInitials.text = if (words.size >= 2) {
                                (words[0][0].toString() + words[1][0].toString()).uppercase()
                            } else {
                                name.take(1).uppercase()
                            }
                        } catch (e: Exception) {
                            itemBinding.tvInitials.text = "U"
                        }

                        // Joined Date
                        if (createdAt > 0) {
                            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                            itemBinding.tvJoinedDate.text = "Joined ${sdf.format(Date(createdAt))}"
                        } else {
                            itemBinding.tvJoinedDate.text = "Joined recently"
                        }

                        binding.llUserList.addView(itemBinding.root)

                        // Update counters
                        totalMembers++
                        when (status.lowercase()) {
                            "active" -> activeCount++
                            "inactive" -> inactiveCount++
                            "banned" -> bannedCount++
                        }
                    }
                }

                // Update Summary Stats
                binding.tvTotalMembers.text = "$totalMembers members"
                binding.tvSummaryTotal.text = totalMembers.toString()
                binding.tvSummaryActive.text = activeCount.toString()
                binding.tvSummaryInactive.text = inactiveCount.toString()
                binding.tvSummaryBanned.text = bannedCount.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}