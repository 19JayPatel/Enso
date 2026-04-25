package com.example.enso.admin.fragments

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
import com.example.enso.admin.adapters.SalonAdapter
import com.example.enso.admin.models.SalonModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.iterator

class AdminSalonsFragment : Fragment() {

    private lateinit var rvSalons: RecyclerView
    private lateinit var adapter: SalonAdapter
    private var allSalonList = ArrayList<SalonModel>()
    private var filteredList = ArrayList<SalonModel>()
    private lateinit var database: DatabaseReference

    private lateinit var tvTotal: TextView
    private lateinit var filterAll: TextView
    private lateinit var filterActive: TextView
    private lateinit var filterPending: TextView
    private lateinit var filterSuspended: TextView

    private var selectedFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_salons, container, false)

        // Initialize Views
        rvSalons = view.findViewById(R.id.rv_salons)
        tvTotal = view.findViewById(R.id.tv_registered_count)
        filterAll = view.findViewById(R.id.filter_all)
        filterActive = view.findViewById(R.id.filter_active)
        filterPending = view.findViewById(R.id.filter_pending)
        filterSuspended = view.findViewById(R.id.filter_suspended)

        adapter = SalonAdapter(filteredList)
        rvSalons.layoutManager = LinearLayoutManager(requireContext())
        rvSalons.adapter = adapter

        // Setup Chips
        filterAll.setOnClickListener {
            selectedFilter = "all"
            filterSalons("all")
            updateChipSelection()
        }
        filterActive.setOnClickListener {
            selectedFilter = "active"
            filterSalons("active")
            updateChipSelection()
        }
        filterPending.setOnClickListener {
            selectedFilter = "pending"
            filterSalons("pending")
            updateChipSelection()
        }
        filterSuspended.setOnClickListener {
            selectedFilter = "suspended"
            filterSalons("suspended")
            updateChipSelection()
        }

        // Initial selection state
        selectedFilter = "all"
        updateChipSelection()

        database = FirebaseDatabase.getInstance().getReference("Salons")
        fetchSalons()

        return view
    }

    private fun updateChipSelection() {
        filterAll.isSelected = selectedFilter == "all"
        filterActive.isSelected = selectedFilter == "active"
        filterPending.isSelected = selectedFilter == "pending"
        filterSuspended.isSelected = selectedFilter == "suspended"
    }

    /**
     * FIX: Optimized fetch logic to calculate dynamic booking and service counts.
     */
    private fun fetchSalons() {
        val salonsRef = FirebaseDatabase.getInstance().getReference("Salons")
        val bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings")
        val servicesRef = FirebaseDatabase.getInstance().getReference("Services")

        salonsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(salonSnapshot: DataSnapshot) {
                val salonMap = HashMap<String, SalonModel>()
                val salonIds = ArrayList<String>()
                val ownerIds = ArrayList<String>()

                for (data in salonSnapshot.children) {
                    val salon = data.getValue(SalonModel::class.java)
                    if (salon != null) {
                        salon.salonId = data.key ?: ""
                        salonMap[salon.salonId] = salon
                        salonIds.add(salon.salonId)
                        ownerIds.add(salon.ownerId)
                    }
                }

                // Load Bookings once
                bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(bookingsSnapshot: DataSnapshot) {
                        val bookingCountMap = HashMap<String, Int>()
                        for (booking in bookingsSnapshot.children) {
                            val salonId = booking.child("salonId").getValue(String::class.java)
                            if (!salonId.isNullOrEmpty()) {
                                bookingCountMap[salonId] = (bookingCountMap[salonId] ?: 0) + 1
                            }
                        }

                        // Load Services once (structured as Services -> ownerId -> serviceId)
                        servicesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(servicesSnapshot: DataSnapshot) {
                                val serviceCountMap = HashMap<String, Int>()
                                for (ownerNode in servicesSnapshot.children) {
                                    val ownerId = ownerNode.key ?: continue
                                    serviceCountMap[ownerId] = ownerNode.childrenCount.toInt()
                                }

                                // Final Update: Attach counts to models
                                allSalonList.clear()
                                for ((salonId, salon) in salonMap) {
                                    salon.bookingsCount = bookingCountMap[salonId] ?: 0
                                    salon.servicesCount = serviceCountMap[salon.ownerId] ?: 0
                                    allSalonList.add(salon)
                                }

                                updateCounts()
                                filterSalons(selectedFilter)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun filterSalons(status: String) {
        filteredList.clear()
        if (status == "all") {
            filteredList.addAll(allSalonList)
        } else {
            for (salon in allSalonList) {
                if (salon.status == status) {
                    filteredList.add(salon)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateCounts() {
        val total = allSalonList.size
        tvTotal.text = "$total registered"
    }

    private fun approveSalon(salonId: String, ownerId: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val updates = HashMap<String, Any>()
        updates["/Salons/$salonId/status"] = "active"
        updates["/Users/$ownerId/status"] = "active"

        rootRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Salon Approved Successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}