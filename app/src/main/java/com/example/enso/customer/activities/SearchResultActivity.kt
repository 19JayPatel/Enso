package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchResultActivity : AppCompatActivity() {

    private lateinit var adapter: SalonAdapter
    private val filteredList = mutableListOf<Salon>()
    private lateinit var tvSalonCount: TextView
    private lateinit var rvSalons: RecyclerView
    private var searchKeyword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_salon)

        // STEP 5: Hide bottom navigation bar inside SearchResultActivity
        // WHY: Search result screen should remain distraction free so users can focus on their search intent without UI clutter.
        // WHAT: Ensuring the bottom navigation (if part of activity) or other distractions are not visible.
        // HOW: Using supportActionBar?.hide() or focusing on the result content to keep the experience streamlined.
        supportActionBar?.hide()

        // STEP 2: Receive keyword using intent.getStringExtra("searchKeyword")
        // WHY: The title should match the search keyword to provide visual confirmation of the user's query and maintain context.
        // WHAT: Extracting the passed keyword and setting it as the header title dynamically.
        // HOW: intent.getStringExtra retrieves the data passed from SearchFragment.
        searchKeyword = intent.getStringExtra("searchKeyword")

        // Initialize Views
        tvSalonCount = findViewById(R.id.tvSalonCount)
        rvSalons = findViewById(R.id.rvSalons)
        val btnBack = findViewById<View>(R.id.btnBack)

        // Find and set the title TextView dynamically (Finding by position as it lacks an ID in activity_all_salon.xml)
        val headerLayout = btnBack.parent as? RelativeLayout
        for (i in 0 until (headerLayout?.childCount ?: 0)) {
            val child = headerLayout?.getChildAt(i)
            if (child is TextView) {
                child.text = searchKeyword ?: "Search Results"
                break
            }
        }

        btnBack.setOnClickListener { finish() }

        // STEP 4: Display filtered salons inside RecyclerView
        // WHY: Reusing the existing SalonAdapter improves performance and ensures a consistent UI across the app with minimal code duplication.
        // WHAT: Initializing RecyclerView with the shared SalonAdapter from the customer adapters package.
        // HOW: We pass the filtered list to the adapter and handle item clicks to navigate to details.
        rvSalons.layoutManager = LinearLayoutManager(this)
        adapter = SalonAdapter(filteredList) { salon ->
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

        // STEP 3: Fetch and Filter salons from Firebase
        fetchAndFilterSalons()
    }

    private fun fetchAndFilterSalons() {
        val keyword = searchKeyword?.lowercase() ?: ""
        val database = FirebaseDatabase.getInstance().getReference("Salons")

        // WHY: Approved filter is required to maintain marketplace safety and prevent unverified or inactive salons from being displayed.
        // WHAT: Querying Firebase for 'active' salons and then filtering by the user's keyword locally.
        // HOW: Firebase filtering on "status" (server-side) ensures data integrity, while local filtering allows for flexible text matching on names.
        database.orderByChild("status").equalTo("active")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    filteredList.clear()
                    for (data in snapshot.children) {
                        val salon = data.getValue(Salon::class.java)
                        if (salon != null) {
                            salon.salonId = data.key ?: ""
                            
                            // Local keyword filtering logic
                            // WHY: Local filtering is necessary for "contains" search as Firebase Realtime Database has limited query capabilities for substring matching.
                            // WHAT: Comparing the salon name with the search keyword in lowercase.
                            if (salon.salonName.lowercase().contains(keyword)) {
                                filteredList.add(salon)
                            }
                        }
                    }

                    // STEP 6: If no salon matches keyword
                    // WHY: Empty state improves UX by clearly communicating to the user that no results were found for their specific query.
                    // WHAT: Checking the size of the filtered list and updating the count text or showing a "No salons found" message.
                    if (filteredList.isEmpty()) {
                        tvSalonCount.text = "No salons found for \"$searchKeyword\""
                        Toast.makeText(this@SearchResultActivity, "No salons found", Toast.LENGTH_SHORT).show()
                    } else {
                        tvSalonCount.text = "Found ${filteredList.size} salons for \"$searchKeyword\""
                    }
                    
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SearchResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
