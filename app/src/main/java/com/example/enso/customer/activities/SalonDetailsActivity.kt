package com.example.enso.customer.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class SalonDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assuming activity_salon_details exists or reusing fragment layout
        setContentView(R.layout.activity_salon_details)

        // Receive data from Intent
        val salonId = intent.getStringExtra("salonId")
        val name = intent.getStringExtra("name")
        val location = intent.getStringExtra("location")
        val rating = intent.getStringExtra("rating")

        // Set to UI
        val tvSalonName = findViewById<TextView>(R.id.tvSalonName)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvRating = findViewById<TextView>(R.id.tvRating)

        tvSalonName.text = name
        tvLocation.text = location
        tvRating.text = rating

        // Back button logic
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}
