package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.example.enso.customer.BookingModel
import com.example.enso.customer.BookingSessionManager
import com.example.enso.databinding.ActivityConfirmBookingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ConfirmBookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmBookingBinding
    private var bookingConfirmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components and display dynamic data
        setupSalonCard()
        setupBookingDate()
        setupSelectedServices()
        setupUI()
    }

    private fun setupSalonCard() {
        val salonName = BookingSessionManager.salonName
        val salonLocation = BookingSessionManager.salonLocation

        val tvInitials = findViewById<TextView>(R.id.tvInitials)
        val tvSalonName = findViewById<TextView>(R.id.tvSalonName)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)

        tvSalonName.text = salonName
        tvLocation.text = salonLocation

        if (salonName.isNotEmpty()) {
            val words = salonName.trim().split(" ")
            if (words.size >= 2) {
                tvInitials.text = (words[0][0].toString() + words[1][0].toString()).uppercase()
            } else {
                tvInitials.text = words[0][0].toString().uppercase()
            }
        } else {
            tvInitials.text = "S"
        }
    }

    private fun setupBookingDate() {
        val bookingDate = BookingSessionManager.bookingDate
        val bookingTime = BookingSessionManager.bookingTime
        binding.tvBookingDate.text = "$bookingDate at $bookingTime"
    }

    private fun setupSelectedServices() {
        val selectedServices = BookingSessionManager.selectedServices
        binding.layoutServicesContainer.removeAllViews()

        var calculatedTotal = 0
        for (service in selectedServices) {
            val rowView = layoutInflater.inflate(
                R.layout.item_selected_service_summary,
                binding.layoutServicesContainer,
                false
            )
            val tvServiceSummaryNameDuration = rowView.findViewById<TextView>(R.id.tvServiceSummaryNameDuration)
            val tvServiceSummaryPrice = rowView.findViewById<TextView>(R.id.tvServiceSummaryPrice)

            tvServiceSummaryNameDuration.text = "${service.serviceName} - ${service.duration} mins"
            tvServiceSummaryPrice.text = "$${service.price}"

            calculatedTotal += service.price?.toIntOrNull() ?: 0
            binding.layoutServicesContainer.addView(rowView)
        }

        binding.tvSubtotal.text = "$$calculatedTotal"
        binding.tvDiscount.text = "$0"
        binding.tvTotalPrice.text = "$$calculatedTotal"
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnConfirm.setOnClickListener {
            saveBookingToFirebase()
        }
    }

    /**
     * FIX: Save booking using the required structure and IDs.
     */
    private fun saveBookingToFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")
        val bookingId = databaseReference.push().key ?: return

        // Collect data from SessionManager
        val customerName = BookingSessionManager.customerName
        val salonId = BookingSessionManager.salonId
        val ownerId = BookingSessionManager.ownerId
        val salonName = BookingSessionManager.salonName
        val bookingDate = BookingSessionManager.bookingDate
        val bookingTime = BookingSessionManager.bookingTime
        
        // Handle services (using first one for ID/Name as per model or comma separated)
        val selectedServices = BookingSessionManager.selectedServices
        val serviceId = selectedServices.firstOrNull()?.serviceId ?: ""
        val serviceName = selectedServices.joinToString(", ") { it.serviceName ?: "" }
        
        val price = selectedServices.sumOf { it.price?.toIntOrNull() ?: 0 }.toString()
        val duration = BookingSessionManager.totalDurationMinutes.toString()

        val booking = BookingModel(
            bookingId = bookingId,
            customerId = currentUserId,
            customerName = customerName,
            salonId = salonId,
            ownerId = ownerId,
            serviceId = serviceId,
            serviceName = serviceName,
            salonName = salonName,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            price = price,
            duration = duration,
            status = "upcoming",
            createdAt = System.currentTimeMillis()
        )

        databaseReference.child(bookingId).setValue(booking)
            .addOnSuccessListener {
                bookingConfirmed = true
                Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show()

                // Unlock slot as it's now officially booked (or slot management logic)
                // BookingSessionManager.reset() // Reset after flow
                
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Booking Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unlockSlot(salonId: String, date: String, time: String) {
        if (salonId.isEmpty() || date.isEmpty() || time.isEmpty()) return
        FirebaseDatabase.getInstance().getReference("SlotLocks")
            .child(salonId).child(date).child(time).removeValue()
    }

    override fun onBackPressed() {
        if (!bookingConfirmed) {
            unlockSlot(BookingSessionManager.salonId, BookingSessionManager.bookingDate, BookingSessionManager.bookingTime)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (!bookingConfirmed) {
            unlockSlot(BookingSessionManager.salonId, BookingSessionManager.bookingDate, BookingSessionManager.bookingTime)
        }
        super.onDestroy()
    }
}
