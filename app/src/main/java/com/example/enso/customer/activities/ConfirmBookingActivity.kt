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
import com.example.enso.customer.activities.MainActivity

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

    /**
     * Sets salon card data dynamically from intent or session manager
     */
    private fun setupSalonCard() {
        val salonName = intent.getStringExtra("salonName") ?: BookingSessionManager.salonName
        val salonLocation = intent.getStringExtra("salonAddress") ?: BookingSessionManager.salonLocation
        val ratingString = intent.getStringExtra("salonRating") ?: ""
        val ratingCount = intent.getStringExtra("salonRatingCount") ?: ""

        val tvInitials = findViewById<TextView>(R.id.tvInitials)
        val tvSalonName = findViewById<TextView>(R.id.tvSalonName)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvRatingValue = findViewById<TextView>(R.id.tvRatingValue)
        val tvRatingCount = findViewById<TextView>(R.id.tvRatingCount)

        tvSalonName.text = salonName
        tvLocation.text = salonLocation

        // Generate Initials for the salon profile placeholder
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

        // Handle rating display logic
        if (ratingString.contains(" ")) {
            val parts = ratingString.split(" ")
            if (parts.size >= 2) {
                tvRatingValue.text = parts[0]
                tvRatingCount.text = parts[1]
            } else {
                tvRatingValue.text = ratingString
                tvRatingCount.text = ""
            }
        } else {
            tvRatingValue.text = ratingString
            tvRatingCount.text = if (ratingCount.isNotEmpty()) "($ratingCount)" else ""
        }
    }

    /**
     * Displays selected date and time from the session manager
     */
    private fun setupBookingDate() {
        val bookingDate = BookingSessionManager.bookingDate
        val bookingTime = BookingSessionManager.bookingTime
        binding.tvBookingDate.text = "$bookingDate at $bookingTime"
    }

    /**
     * Dynamically adds selected services to the summary list and calculates total price breakdown
     */
    private fun setupSelectedServices() {
        val selectedServices = BookingSessionManager.selectedServices
        binding.layoutServicesContainer.removeAllViews()

        // This variable calculates the subtotal by adding up the price of each selected service
        // subtotal is sum of selected services before discount applied
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

        // This variable stores the subtotal (sum of all selected services)
        // It is saved in Firebase so we can track the base cost before any offers
        val subtotal = calculatedTotal

        // This variable stores the discount amount, which is currently set to 0 by default
        // This ensures future coupon system compatibility where we can subtract real values
        val discount = 0

        // This variable calculates the grand total by subtracting the discount from the subtotal
        // This is the final price that will be saved in Firebase and shown to the user
        val grandTotal = subtotal - discount

        binding.tvSubtotal.text = "$$subtotal"
        binding.tvDiscount.text = "$0"
        
        // Currency symbol is UI-only formatting to make it readable for users
        // Database must store pure integer values for easy math operations later
        binding.tvTotalPrice.text = "$$grandTotal"
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
     * Core logic to save the booking data to Firebase Realtime Database with upgraded pricing structure
     */
    private fun saveBookingToFirebase() {
        // STEP 1: Get current logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // If NULL: Redirect user to LoginActivity
        if (userId == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        // STEP 2: Get booking data from BookingSessionManager and Intent
        val salonName = intent.getStringExtra("salonName") ?: BookingSessionManager.salonName
        val selectedServicesList = BookingSessionManager.selectedServices
        val selectedDate = BookingSessionManager.bookingDate
        val selectedTime = BookingSessionManager.bookingTime
        val salonId = BookingSessionManager.salonId
        val paymentMethod = "Pay at Salon"
        val paymentStatus = "pending"

        // --- UPDATED PRICE BREAKDOWN LOGIC ---
        
        // This variable calculates the sum of all services chosen by the customer
        // subtotal is the sum of selected services before discount applied
        val subtotal = selectedServicesList.sumOf { it.price?.toIntOrNull() ?: 0 }
        
        // This variable holds the discount value, starting at 0 for now
        // discount default value 0 allows future coupon system compatibility
        val discount = 0
        
        // This variable calculates the final amount the customer needs to pay
        // grandTotal calculation logic ensures we subtract any discounts from the subtotal
        val grandTotal = subtotal - discount
        
        // ---------------------------------------

        // Convert selectedServices list into comma separated string
        val servicesString = selectedServicesList.joinToString(", ") { it.serviceName ?: "" }

        // STEP 3: Generate bookingId
        val databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")
        val bookingId = databaseReference.push().key ?: return

        // STEP 4: Create BookingModel object with the new pricing breakdown
        // Firebase structure upgrade supports coupons and a robust pricing engine for the marketplace
        val booking = BookingModel(
            bookingId = bookingId,
            userId = userId,
            salonId = salonId,
            salonName = salonName,
            services = servicesString,
            subtotal = subtotal,
            discount = discount,
            grandTotal = grandTotal,
            bookingDate = selectedDate,
            bookingTime = selectedTime,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            status = "confirmed",
            createdAt = System.currentTimeMillis()
        )

        // WHY:
        // BookingModel should store snapshot of salon data at booking time.
        // This avoids additional Firebase queries later when displaying booking cards.
        //
        // This follows production booking-app architecture used by real platforms.
        booking.salonLocation = BookingSessionManager.salonLocation
        booking.salonImageUrl = BookingSessionManager.salonImageUrl

        // STEP 5: Save booking inside Firebase: Bookings -> bookingId
        // This pricing breakdown structure supports future marketplace features and tracking
        databaseReference.child(bookingId).setValue(booking)
            .addOnSuccessListener {
                // STEP 6: If booking saved successfully
                bookingConfirmed = true
                Toast.makeText(this, "Booking Confirmed Successfully", Toast.LENGTH_SHORT).show()

                // Clear BookingSessionManager
                BookingSessionManager.reset()

                // Navigate user to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // STEP 7: If Firebase save fails
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Utility function to unlock time slots if booking is cancelled/destroyed
     */
    private fun unlockSlot(salonId: String, date: String, time: String) {
        if (salonId.isEmpty() || date.isEmpty() || time.isEmpty()) return
        val lockRef = FirebaseDatabase.getInstance().getReference("SlotLocks").child(salonId).child(date).child(time)
        lockRef.removeValue()
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
