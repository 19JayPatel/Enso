package com.example.enso.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.adapters.AllSalonActivity
import com.example.enso.customer.activities.MainActivity
import com.google.firebase.database.*

/**
 * Simple Data model for Salon Home
 */
data class SalonHome(
    var salonId: String = "",
    var salonName: String = "",
    var address: Map<String, String>? = null,
    var rating: String = "4.7 (312)"
)

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var rvSalons: RecyclerView
    private lateinit var salonAdapter: SalonHomeAdapter
    private val salonList = mutableListOf<SalonHome>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Search Bar logic
        val llSearchBar = view.findViewById<LinearLayout>(R.id.llSearchBar)
        llSearchBar.setOnClickListener {
            (activity as? MainActivity)?.switchToSearch()
        }

        // 2. View All Salons click listener
        val tvViewAll = view.findViewById<TextView>(R.id.tv_all_salon)
        tvViewAll.setOnClickListener {
            val intent = Intent(requireContext(), AllSalonActivity::class.java)
            startActivity(intent)
        }

        // 3. Setup RecyclerView for Salons (Showing only 2)
        rvSalons = view.findViewById(R.id.rvSalons)
        rvSalons.layoutManager = LinearLayoutManager(requireContext())
        salonAdapter = SalonHomeAdapter(salonList) { salon ->
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("openFragment", "salonDetails")
            intent.putExtra("salonId", salon.salonId)
            intent.putExtra("name", salon.salonName)
            val city = salon.address?.get("city") ?: "Unknown"
            val country = salon.address?.get("country") ?: "Location"
            intent.putExtra("location", "$city, $country")
            intent.putExtra("rating", salon.rating)
            startActivity(intent)
        }
        rvSalons.adapter = salonAdapter

        // 4. Fetch 2 Salons from Firebase
        fetchSalons()
    }

    private fun fetchSalons() {
        val database = FirebaseDatabase.getInstance().getReference("Salons")
        // Limit to 2 as requested
        database.limitToFirst(2).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                salonList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val salon = data.getValue(SalonHome::class.java)
                        if (salon != null) {
                            salon.salonId = data.key ?: ""
                            salonList.add(salon)
                        }
                    }
                    salonAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to load salons", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Beginner-friendly Adapter for Home Salons
     */
    inner class SalonHomeAdapter(
        private val salons: List<SalonHome>,
        private val onItemClick: (SalonHome) -> Unit
    ) : RecyclerView.Adapter<SalonHomeAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvInitials: TextView = view.findViewById(R.id.tvInitials)
            val tvName: TextView = view.findViewById(R.id.tvSalonName)
            val tvLocation: TextView = view.findViewById(R.id.tvLocation)
            val tvRatingValue: TextView = view.findViewById(R.id.tvRatingValue)
            val tvRatingCount: TextView = view.findViewById(R.id.tvRatingCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_salon_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val salon = salons[position]
            holder.tvName.text = salon.salonName
            
            // Get Initials (e.g., Hair Avenue -> HA)
            val name = salon.salonName
            holder.tvInitials.text = if (name.isNotEmpty()) {
                val words = name.trim().split(" ")
                if (words.size >= 2) "${words[0][0]}${words[1][0]}".uppercase()
                else words[0][0].toString().uppercase()
            } else "S"

            // Format Location
            val city = salon.address?.get("city") ?: "Unknown"
            val country = salon.address?.get("country") ?: "Location"
            holder.tvLocation.text = "$city, $country"

            // Split Rating "4.7 (312)" into two text views
            val ratingStr = salon.rating
            if (ratingStr.contains(" ")) {
                val parts = ratingStr.split(" ")
                holder.tvRatingValue.text = parts[0]
                holder.tvRatingCount.text = parts[1]
            } else {
                holder.tvRatingValue.text = ratingStr
                holder.tvRatingCount.text = ""
            }

            holder.itemView.setOnClickListener { onItemClick(salon) }
        }

        override fun getItemCount(): Int = salons.size
    }
}
