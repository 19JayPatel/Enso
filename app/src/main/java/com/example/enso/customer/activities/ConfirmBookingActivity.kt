package com.example.enso.customer.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.databinding.ActivityConfirmBookingBinding

class ConfirmBookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Back button behavior
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Confirm button behavior
        binding.btnConfirm.setOnClickListener {
            Toast.makeText(this, "Appointment Confirmed", Toast.LENGTH_SHORT).show()
        }
        
        // Initializing with sample data as per requirements
        binding.tvStylistName.text = "John Doe"
        binding.tvStylistRole.text = "Hair Specialist"
    }
}