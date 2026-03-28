package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.customer.adapters.DateAdapter
import com.example.enso.customer.adapters.DateModel
import com.example.enso.customer.adapters.TimeSlotAdapter
import com.example.enso.customer.adapters.TimeSlotModel
import com.example.enso.databinding.ActivityDateTimeBinding

class DateTimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDateTimeBinding
    private var isDateSelected = false
    private var isTimeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDateSelection()
        setupTimeSelection()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            val intent = Intent(this, ConfirmBookingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDateSelection() {
        val dates = listOf(
            DateModel("TUE", "Sep 9", "40 mins"),
            DateModel("WED", "Sep 10", "40 mins"),
            DateModel("THU", "Sep 11", "40 mins"),
            DateModel("", "", "", isMoreDates = true)
        )

        val adapter = DateAdapter(dates) {
            isDateSelected = true
            updateButtonState()
        }
        binding.rvDates.adapter = adapter
    }

    private fun setupTimeSelection() {
        val timeSlots = listOf(
            TimeSlotModel("9:00 AM"),
            TimeSlotModel("9:30 AM"),
            TimeSlotModel("10:00 AM"),
            TimeSlotModel("10:30 AM"),
            TimeSlotModel("11:00 AM"),
            TimeSlotModel("11:30 AM"),
            TimeSlotModel("12:00 PM")
        )

        val adapter = TimeSlotAdapter(timeSlots) {
            isTimeSelected = true
            updateButtonState()
        }
        binding.rvTimeSlots.adapter = adapter
    }

    private fun updateButtonState() {
        if (isDateSelected && isTimeSelected) {
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.alpha = 1.0f
        } else {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.alpha = 0.5f
        }
    }
}