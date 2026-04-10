package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class AddServicePricingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_pricing)

        // Initialize Views
        val btnBackHeader = findViewById<android.widget.ImageView>(R.id.btnBackHeader)
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnNext = findViewById<TextView>(R.id.btnNext)

        // Header Back Button
        btnBackHeader.setOnClickListener {
            finish()
        }

        // Bottom Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Next Button - Navigate to Step 3 (Review)
        btnNext.setOnClickListener {
            val intent = Intent(this, AddServiceReviewActivity::class.java)
            startActivity(intent)
        }
    }
}
