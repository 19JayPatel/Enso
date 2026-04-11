package com.example.enso.admin

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
import com.google.firebase.database.*

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

    private fun fetchSalons() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allSalonList.clear()
                for (data in snapshot.children) {
                    val salon = data.getValue(SalonModel::class.java)
                    if (salon != null) {
                        salon.salonId = data.key ?: ""
                        allSalonList.add(salon)
                    }
                }
                updateCounts()
                filterSalons(selectedFilter)
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
}