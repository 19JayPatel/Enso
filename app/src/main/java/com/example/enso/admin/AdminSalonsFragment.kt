package com.example.enso.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class AdminSalonsFragment : Fragment() {

    private lateinit var rvSalons: RecyclerView
    private lateinit var salonAdapter: SalonAdapter
    private lateinit var salonList: ArrayList<SalonModel>
    private lateinit var displayList: ArrayList<SalonModel>

    private lateinit var tvRegisteredCount: TextView
    private lateinit var etSearch: EditText

    private lateinit var filterAll: TextView
    private lateinit var filterActive: TextView
    private lateinit var filterPending: TextView
    private lateinit var filterSuspended: TextView

    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_salons, container, false)

        // Initialize Views
        rvSalons = view.findViewById(R.id.rv_salons)
        tvRegisteredCount = view.findViewById(R.id.tv_registered_count)
        etSearch = view.findViewById(R.id.et_search)

        filterAll = view.findViewById(R.id.filter_all)
        filterActive = view.findViewById(R.id.filter_active)
        filterPending = view.findViewById(R.id.filter_pending)
        filterSuspended = view.findViewById(R.id.filter_suspended)

        // Setup RecyclerView
        salonList = ArrayList()
        displayList = ArrayList()
        salonAdapter = SalonAdapter(displayList)
        rvSalons.layoutManager = LinearLayoutManager(requireContext())
        rvSalons.adapter = salonAdapter

        // Firebase Setup
        dbRef = FirebaseDatabase.getInstance().getReference("Salons")
        fetchSalons()

        // Search Logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter Click Listeners
        filterAll.setOnClickListener { updateFilterUI(filterAll); filterByStatus("All") }
        filterActive.setOnClickListener { updateFilterUI(filterActive); filterByStatus("Active") }
        filterPending.setOnClickListener { updateFilterUI(filterPending); filterByStatus("Pending") }
        filterSuspended.setOnClickListener { updateFilterUI(filterSuspended); filterByStatus("Suspended") }

        return view
    }

    private fun fetchSalons() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                salonList.clear()
                for (postSnapshot in snapshot.children) {
                    val salon = postSnapshot.getValue(SalonModel::class.java)
                    if (salon != null) {
                        salonList.add(salon)
                    }
                }
                tvRegisteredCount.text = "${salonList.size} registered"
                displayList.clear()
                displayList.addAll(salonList)
                salonAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterSearch(query: String) {
        val filtered = salonList.filter {
            it.salonName?.lowercase(Locale.ROOT)?.contains(query.lowercase(Locale.ROOT)) == true ||
                    it.ownerFirstName?.lowercase(Locale.ROOT)
                        ?.contains(query.lowercase(Locale.ROOT)) == true ||
                    it.ownerLastName?.lowercase(Locale.ROOT)
                        ?.contains(query.lowercase(Locale.ROOT)) == true
        }
        salonAdapter.updateList(filtered)
    }

    private fun filterByStatus(status: String) {
        if (status == "All") {
            salonAdapter.updateList(salonList)
        } else {
            val filtered = salonList.filter { it.status == status }
            salonAdapter.updateList(filtered)
        }
    }

    private fun updateFilterUI(selected: TextView) {
        val filters = listOf(filterAll, filterActive, filterPending, filterSuspended)
        for (f in filters) {
            if (f == selected) {
                f.setBackgroundResource(R.drawable.bg_white_rounded_12)
                f.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF1A1A1A.toInt())
                f.setTextColor(android.graphics.Color.WHITE)
            } else {
                f.setBackgroundResource(R.drawable.bg_white_rounded_12)
                f.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                f.setTextColor(0xFF8E8E8E.toInt())
            }
        }
    }
}