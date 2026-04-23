package com.example.enso.owner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.example.enso.customer.BookingModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Salon Owner Bookings Management Fragment.
 * This fragment allows salon owners to view and manage bookings specific to their salon.
 * It fetches data dynamically from Firebase Realtime Database.
 */
class SalonOwnerBookingsFragment : Fragment() {

    private lateinit var rvBookings: RecyclerView
    // WHY: Use OwnerBookingsAdapter as requested to handle the salon owner's view of bookings.
    private lateinit var adapter: OwnerBookingsAdapter

    // STEP 4: CREATE LIST
    // WHY: We use ArrayList to store bookings as it's easy to clear and update dynamically from Firebase.
    private var allBookings = ArrayList<BookingModel>()

    private var ownerSalonId: String? = null
    private var currentTab = "Upcoming"
    
    // STEP 7:
    // WHAT: read argument highlightBookingId
    // WHY: Identifies booking to highlight visually.
    private var highlightBookingId: String? = null

    // UI Elements for Tabs
    private lateinit var tvUpcoming: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvCancelled: TextView
    private lateinit var lineUpcoming: View
    private lateinit var lineCompleted: View
    private lateinit var lineCancelled: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_bookings, container, false)

        // STEP 7:
        // WHAT: Read argument highlightBookingId from Bundle
        // WHY: Identifies booking to highlight visually.
        highlightBookingId = arguments?.getString("highlightBookingId")

        // Initialize UI Elements
        rvBookings = view.findViewById(R.id.rvBookings)
        tvUpcoming = view.findViewById(R.id.tvUpcoming)
        tvCompleted = view.findViewById(R.id.tvCompleted)
        tvCancelled = view.findViewById(R.id.tvCancelled)
        lineUpcoming = view.findViewById(R.id.lineUpcoming)
        lineCompleted = view.findViewById(R.id.lineCompleted)
        lineCancelled = view.findViewById(R.id.lineCancelled)

        // STEP 1: GET CURRENT OWNER ID
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid

        if (ownerId == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return view
        }

        // STEP 4: ATTACH ADAPTER
        // WHAT: Initialize adapter with highlightBookingId
        // WHY: Pass the ID to highlight to the adapter.
        rvBookings.layoutManager = LinearLayoutManager(requireContext())
        adapter = OwnerBookingsAdapter(allBookings, highlightBookingId)
        rvBookings.adapter = adapter

        // STEP 2: GET OWNER SALON ID FROM FIREBASE
        fetchOwnerSalonId(ownerId)

        // Setup Tab Clicks
        view.findViewById<View>(R.id.tabUpcoming).setOnClickListener { selectTab("Upcoming") }
        view.findViewById<View>(R.id.tabCompleted).setOnClickListener { selectTab("Completed") }
        view.findViewById<View>(R.id.tabCancelled).setOnClickListener { selectTab("Cancelled") }

        return view
    }

    private fun fetchOwnerSalonId(ownerId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
        userRef.child("salonId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ownerSalonId = snapshot.getValue(String::class.java)

                if (ownerSalonId != null) {
                    loadBookingsFromFirebase()
                } else {
                    Toast.makeText(requireContext(), "Salon profile not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load owner data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBookingsFromFirebase() {
        val bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings")

        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allBookings.clear()

                for (snapshot in dataSnapshot.children) {
                    val booking = snapshot.getValue(BookingModel::class.java)

                    if (booking != null && booking.salonId == ownerSalonId) {
                        allBookings.add(booking)
                    }
                }

                filterBookings(currentTab)
                
                // Scroll to highlighted booking if exists
                highlightBookingId?.let { id ->
                    val index = allBookings.indexOfFirst { it.bookingId == id }
                    if (index != -1) {
                        rvBookings.post {
                            rvBookings.smoothScrollToPosition(index)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selectTab(tab: String) {
        currentTab = tab

        val gray = Color.parseColor("#8E8E8E")
        val brown = Color.parseColor("#A37551")

        tvUpcoming.setTextColor(gray)
        tvCompleted.setTextColor(gray)
        tvCancelled.setTextColor(gray)
        tvUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvCompleted.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvCancelled.setTypeface(null, android.graphics.Typeface.NORMAL)
        lineUpcoming.setBackgroundColor(Color.TRANSPARENT)
        lineCompleted.setBackgroundColor(Color.TRANSPARENT)
        lineCancelled.setBackgroundColor(Color.TRANSPARENT)

        when (tab) {
            "Upcoming" -> {
                tvUpcoming.setTextColor(brown)
                tvUpcoming.setTypeface(null, android.graphics.Typeface.BOLD)
                lineUpcoming.setBackgroundColor(brown)
            }
            "Completed" -> {
                tvCompleted.setTextColor(brown)
                tvCompleted.setTypeface(null, android.graphics.Typeface.BOLD)
                lineCompleted.setBackgroundColor(brown)
            }
            "Cancelled" -> {
                tvCancelled.setTextColor(brown)
                tvCancelled.setTypeface(null, android.graphics.Typeface.BOLD)
                lineCancelled.setBackgroundColor(brown)
            }
        }

        filterBookings(tab)
    }

    private fun filterBookings(tab: String) {
        val filteredList = when (tab) {
            "Upcoming" -> allBookings.filter {
                it.status.equals("confirmed", ignoreCase = true) ||
                it.status.equals("pending", ignoreCase = true)
            }
            "Completed" -> allBookings.filter { it.status.equals("completed", ignoreCase = true) }
            "Cancelled" -> allBookings.filter { it.status.equals("cancelled", ignoreCase = true) }
            else -> allBookings
        }
        
        adapter.updateList(filteredList)
    }
}
