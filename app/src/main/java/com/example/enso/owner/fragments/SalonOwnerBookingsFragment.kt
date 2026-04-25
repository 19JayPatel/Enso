package com.example.enso.owner.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.enso.customer.models.BookingModel
import com.example.enso.owner.adapters.OwnerBookingsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SalonOwnerBookingsFragment : Fragment() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var adapter: OwnerBookingsAdapter
    private var allBookings = ArrayList<BookingModel>()
    private var currentTab = "Upcoming"
    private var highlightBookingId: String? = null

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

        highlightBookingId = arguments?.getString("highlightBookingId")

        rvBookings = view.findViewById(R.id.rvBookings)
        tvUpcoming = view.findViewById(R.id.tvUpcoming)
        tvCompleted = view.findViewById(R.id.tvCompleted)
        tvCancelled = view.findViewById(R.id.tvCancelled)
        lineUpcoming = view.findViewById(R.id.lineUpcoming)
        lineCompleted = view.findViewById(R.id.lineCompleted)
        lineCancelled = view.findViewById(R.id.lineCancelled)

        val ownerId = FirebaseAuth.getInstance().currentUser?.uid

        if (ownerId == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return view
        }

        rvBookings.layoutManager = LinearLayoutManager(requireContext())
        adapter = OwnerBookingsAdapter(allBookings, highlightBookingId)
        rvBookings.adapter = adapter

        // Load bookings directly using ownerId filter
        loadBookingsFromFirebase(ownerId)

        view.findViewById<View>(R.id.tabUpcoming).setOnClickListener { selectTab("Upcoming") }
        view.findViewById<View>(R.id.tabCompleted).setOnClickListener { selectTab("Completed") }
        view.findViewById<View>(R.id.tabCancelled).setOnClickListener { selectTab("Cancelled") }

        return view
    }

    private fun loadBookingsFromFirebase(ownerId: String) {
        // FIX: Direct filter by ownerId as required
        val bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings")
        val query = bookingsRef.orderByChild("ownerId").equalTo(ownerId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allBookings.clear()

                for (snapshot in dataSnapshot.children) {
                    val booking = snapshot.getValue(BookingModel::class.java)
                    if (booking != null) {
                        allBookings.add(booking)
                    }
                }

                filterBookings(currentTab)

                highlightBookingId?.let { id ->
                    val index = allBookings.indexOfFirst { it.bookingId == id }
                    if (index != -1) {
                        rvBookings.post { rvBookings.smoothScrollToPosition(index) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show()
                }
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
        tvUpcoming.setTypeface(null, Typeface.NORMAL)
        tvCompleted.setTypeface(null, Typeface.NORMAL)
        tvCancelled.setTypeface(null, Typeface.NORMAL)
        lineUpcoming.setBackgroundColor(Color.TRANSPARENT)
        lineCompleted.setBackgroundColor(Color.TRANSPARENT)
        lineCancelled.setBackgroundColor(Color.TRANSPARENT)

        when (tab) {
            "Upcoming" -> {
                tvUpcoming.setTextColor(brown)
                tvUpcoming.setTypeface(null, Typeface.BOLD)
                lineUpcoming.setBackgroundColor(brown)
            }
            "Completed" -> {
                tvCompleted.setTextColor(brown)
                tvCompleted.setTypeface(null, Typeface.BOLD)
                lineCompleted.setBackgroundColor(brown)
            }
            "Cancelled" -> {
                tvCancelled.setTextColor(brown)
                tvCancelled.setTypeface(null, Typeface.BOLD)
                lineCancelled.setBackgroundColor(brown)
            }
        }
        filterBookings(tab)
    }

    private fun filterBookings(tab: String) {
        val filteredList = when (tab) {
            "Upcoming" -> allBookings.filter { it.status.equals("upcoming", ignoreCase = true) }
            "Completed" -> allBookings.filter { it.status.equals("completed", ignoreCase = true) }
            "Cancelled" -> allBookings.filter { it.status.equals("cancelled", ignoreCase = true) }
            else -> allBookings
        }
        adapter.updateList(filteredList)
    }
}