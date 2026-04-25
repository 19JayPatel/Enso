package com.example.enso.owner.fragments

import android.content.Intent
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
import com.example.enso.owner.adapters.UpcomingAppointmentAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalonOwnerDashboardFragment : Fragment() {

    private lateinit var rvAppointments: RecyclerView
    private lateinit var adapter: UpcomingAppointmentAdapter
    private lateinit var tvSalonName: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvEmptyAppointments: TextView

    // WHAT: Inflate layout inside onCreateView()
    // WHY: Fragments must return the view hierarchy in this method.
    // HOW: Use standard inflater to load fragment_salon_owner_dashboard XML.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_salon_owner_dashboard, container, false)
    }

    // WHAT: Initialize UI components and Firebase logic inside onViewCreated()
    // WHY: UI references must be initialized after layout is attached to avoid null pointers.
    // HOW: Find views by ID and setup RecyclerView adapter and listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI Elements
        tvSalonName = view.findViewById(R.id.tvSalonName)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvEmptyAppointments = view.findViewById(R.id.tvEmptyAppointments)
        rvAppointments = view.findViewById(R.id.rvDashboardAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(requireContext())

        // STEP 4:
        // WHAT: Implement adapter listener and initialize adapter
        // WHY: Fragment controls navigation logic.
        // HOW: Pass OnViewClickListener implementation to UpcomingAppointmentAdapter.
        adapter = UpcomingAppointmentAdapter(
            mutableListOf(),
            object : UpcomingAppointmentAdapter.OnViewClickListener {
                override fun onViewClicked(bookingId: String) {
                    navigateToBookingsFragment(bookingId)
                }
            }
        )
        rvAppointments.adapter = adapter

        // STEP 3:
        // WHAT: Fetch logged-in salon owner UID
        // WHY: Confirms correct owner session is active.
        // HOW: Retrieve UID from FirebaseAuth instance.
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        /**
        *Toast.makeText(requireContext(), "Owner UID: $ownerId", Toast.LENGTH_SHORT).show()
        */

        val database = FirebaseDatabase.getInstance().reference

        // STEP 4:
        // WHAT: Fetch salonId from Firebase Users node
        // WHY: Dashboard bookings depend on salonId filtering.
        // HOW: Access path Users/{ownerId}/salonId in Realtime Database.
        database.child("Users").child(ownerId).child("salonId").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ownerSalonId = snapshot.getValue(String::class.java)
                if (ownerSalonId != null) {
//                    Toast.makeText(requireContext(), "Salon ID loaded", Toast.LENGTH_SHORT).show()

                    // Fetch Salon details for UI header
                    fetchSalonDetails(ownerSalonId)

                    // STEP 5:
                    // WHAT: Attach realtime listener to Bookings node
                    // WHY: Dashboard must update automatically when bookings change.
                    // HOW: Use addValueEventListener on "Bookings" child.
                    database.child("Bookings").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // STEP 6:
                            // WHAT: Filter bookings for current salon and today's date
                            // WHY: Dashboard must show only today's appointments for this salon owner.
                            // HOW: Iterate snapshot, check salonId and compare bookingDate with today's date.
                            val todayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                                Date()
                            )
                            val filteredBookings = mutableListOf<BookingModel>()

                            for (bookingSnap in snapshot.children) {
                                val booking = bookingSnap.getValue(BookingModel::class.java)
                                if (booking != null && booking.salonId == ownerSalonId && booking.bookingDate == todayDate) {
                                    // STEP 7:
                                    // WHAT: Store filtered bookings inside MutableList
                                    // WHY: Adapter requires dataset list for RecyclerView rendering.
                                    // HOW: Add matching BookingModel objects to filteredBookings list.
                                    filteredBookings.add(booking)
                                }
                            }

                            // STEP 8:
                            // WHAT: Update adapter with filtered bookings
                            // WHY: Confirms Firebase filtering working correctly and refreshes UI.
                            // HOW: Call adapter.updateList(filteredBookings).
                            adapter.updateList(filteredBookings)
                            /**Toast.makeText(
                                requireContext(),
                                "Bookings fetched: ${filteredBookings.size}",
                                Toast.LENGTH_SHORT
                            ).show()*/

                            if (filteredBookings.isEmpty()) {
                                tvEmptyAppointments.visibility = View.VISIBLE
                                rvAppointments.visibility = View.GONE
                            } else {
                                tvEmptyAppointments.visibility = View.GONE
                                rvAppointments.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // STEP 5:
    // WHAT: Create navigation function to BookingsFragment
    // WHY: Switch screen from dashboard to bookings list and pass data.
    // HOW: Use Bundle to transfer bookingId and parentFragmentManager to replace fragment.
    private fun navigateToBookingsFragment(bookingId: String) {
        val bundle = Bundle()
        bundle.putString("highlightBookingId", bookingId)

        // STEP 6:
        // WHAT: Open BookingsFragment using FragmentManager
        // WHY: Switch screen from dashboard to bookings list.
        // HOW: Use replace transaction on fragment_container (matching SalonOwnerMainActivity).
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SalonOwnerBookingsFragment().apply {
                arguments = bundle
            })
            .addToBackStack(null)
            .commit()
    }

    private fun fetchSalonDetails(salonId: String) {
        // WHAT: Fetch salon details using salonId
        // WHY: To display salon name and location in the dashboard header.
        // HOW: Retrieve data from "Salons/{salonId}" node.
        FirebaseDatabase.getInstance().getReference("Salons").child(salonId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val salonName = snapshot.child("salonName").getValue(String::class.java)
                        val street = snapshot.child("address").child("street").getValue(String::class.java)
                        val state = snapshot.child("address").child("state").getValue(String::class.java)
                        val country = snapshot.child("address").child("country").getValue(String::class.java)

                        val location = "$street, $state, $country"
                        tvSalonName.text = salonName ?: "Salon Name"
                        tvLocation.text = if (!street.isNullOrEmpty()) location else "Location not set"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}