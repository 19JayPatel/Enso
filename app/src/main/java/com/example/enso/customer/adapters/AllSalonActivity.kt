package com.example.enso.customer.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Data model for Salon - Matches your Firebase Structure exactly
 */
data class Salon(
    var salonId: String = "",
    var salonName: String = "",
    var address: Map<String, String>? = null,
    var rating: String = "4.7 (312)" // Default if not in DB yet
)

/**
 * AllSalonActivity shows a list of all available salons.
 * Fetches data dynamically from Firebase Realtime Database.
 */
class AllSalonActivity : AppCompatActivity() {

    private lateinit var adapter: SalonAdapter
    private val salonList = mutableListOf<Salon>()
    private lateinit var tvSalonCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_salon)

        // Initialize Views
        tvSalonCount = findViewById(R.id.tvSalonCount)
        val btnBack = findViewById<View>(R.id.btnBack)
        val rvSalons = findViewById<RecyclerView>(R.id.rvSalons)

        // Setup Back Button click
        btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        rvSalons.layoutManager = LinearLayoutManager(this)
        adapter = SalonAdapter(salonList) { salon ->
            // Open MainActivity and load SalonDetailsFragment on item click
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "salonDetails")
            intent.putExtra("salonId", salon.salonId)
            intent.putExtra("name", salon.salonName)
            val city = salon.address?.get("city") ?: "Unknown"
            val country = salon.address?.get("country") ?: "Location"
            intent.putExtra("location", "$city, $country")
            intent.putExtra("rating", salon.rating)
            startActivity(intent)
        }
        rvSalons.adapter = adapter

        // 🔥 1. INITIALIZE FIREBASE
        val database = FirebaseDatabase.getInstance().getReference("Salons")

        // 🔥 2. FETCH DATA FROM FIREBASE
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                salonList.clear()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val salon = data.getValue(Salon::class.java)
                        if (salon != null) {
                            salon.salonId = data.key ?: "" // Set Firebase key as salonId
                            salonList.add(salon)
                        }
                    }
                    
                    // 🔥 Update Dynamic Count
                    tvSalonCount.text = "Showing ${salonList.size} salons near you"
                    
                    adapter.notifyDataSetChanged()
                } else {
                    tvSalonCount.text = "Showing 0 salons near you"
                    Toast.makeText(this@AllSalonActivity, "No salons found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllSalonActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

/**
 * RecyclerView Adapter for Salon items
 */
class SalonAdapter(
    private val salons: List<Salon>,
    private val onItemClick: (Salon) -> Unit
) : RecyclerView.Adapter<SalonAdapter.SalonViewHolder>() {

    class SalonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvName: TextView = view.findViewById(R.id.tvSalonName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvRatingValue: TextView = view.findViewById(R.id.tvRatingValue)
        val tvRatingCount: TextView = view.findViewById(R.id.tvRatingCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_salon_list, parent, false)
        return SalonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalonViewHolder, position: Int) {
        val salon = salons[position]
        
        // 🔥 Bind data to views using exact Firebase keys
        holder.tvName.text = salon.salonName
        holder.tvInitials.text = getInitials(salon.salonName)
        
        // Handle Address (City, Country)
        val city = salon.address?.get("city") ?: "Unknown"
        val country = salon.address?.get("country") ?: "Location"
        holder.tvLocation.text = "$city, $country"
        
        // Split Rating "4.7 (312)"
        val ratingStr = salon.rating
        if (ratingStr.contains(" ")) {
            val parts = ratingStr.split(" ")
            holder.tvRatingValue.text = parts[0]
            holder.tvRatingCount.text = parts[1]
        } else {
            holder.tvRatingValue.text = ratingStr
            holder.tvRatingCount.text = ""
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(salon)
        }
    }

    override fun getItemCount(): Int = salons.size

    /**
     * 🔥 INITIALS LOGIC
     */
    private fun getInitials(name: String): String {
        if (name.isEmpty()) return "S"
        val words = name.trim().split(" ")
        return if (words.size >= 2) {
            "${words[0][0]}${words[1][0]}".uppercase()
        } else {
            words[0][0].toString().uppercase()
        }
    }
}
