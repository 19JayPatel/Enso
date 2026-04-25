package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.example.enso.customer.models.BookingModel
import com.example.enso.customer.BookingSessionManager
import com.example.enso.databinding.ActivityConfirmBookingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
            checkSlotAvailabilityAndBook()
        }
    }

    /**
     * FIX: Check slot conflict before booking.
     */
    private fun checkSlotAvailabilityAndBook() {
        val salonId = BookingSessionManager.salonId
        val bookingDate = BookingSessionManager.bookingDate
        val bookingTime = BookingSessionManager.bookingTime

        val databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")

        // Search for existing bookings with same salon, date and time
        databaseReference.orderByChild("salonId").equalTo(salonId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isSlotTaken = false

                    for (bookingSnap in snapshot.children) {
                        val existingDate = bookingSnap.child("bookingDate").getValue(String::class.java)
                        val existingTime = bookingSnap.child("bookingTime").getValue(String::class.java)
                        val existingStatus = bookingSnap.child("status").getValue(String::class.java)

                        if (existingDate == bookingDate && existingTime == bookingTime && existingStatus != "cancelled") {
                            isSlotTaken = true
                            break
                        }
                    }

                    if (isSlotTaken) {
                        Toast.makeText(this@ConfirmBookingActivity, "This time slot is already booked", Toast.LENGTH_LONG).show()
                    } else {
                        saveBookingToFirebase()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ConfirmBookingActivity, "Validation Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

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

        val customerName = BookingSessionManager.customerName
        val salonId = BookingSessionManager.salonId
        val ownerId = BookingSessionManager.ownerId
        val salonName = BookingSessionManager.salonName
        val bookingDate = BookingSessionManager.bookingDate
        val bookingTime = BookingSessionManager.bookingTime
        
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
