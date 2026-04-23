package com.example.enso.customer.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.customer.BookingModel
import com.example.enso.databinding.ActivityReceiptBinding
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

/**
 * ReceiptActivity displays the dynamic receipt for a specific booking.
 * It fetches data from multiple Firebase nodes (Bookings, Users, Salons)
 * to show complete information to the customer.
 */
class ReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptBinding
    private lateinit var database: DatabaseReference
    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WHY bookingId passed through Intent:
        // The bookingId is the unique identifier for the receipt we need to display.
        // Passing it via Intent allows this Activity to be reusable for any booking.
        bookingId = intent.getStringExtra("BOOKING_ID")

        if (bookingId == null) {
            Toast.makeText(this, "Error: Booking ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference

        setupClickListeners()
        fetchBookingDetails()
        generateQRCode(bookingId!!)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnDownloadReceipt.setOnClickListener {
            // Requirement 7: Temporarily show Toast for download feature
            Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * HOW realtime Firebase listener works:
     * We use addValueEventListener to listen for changes in the booking data.
     * If the status or details change, the UI updates automatically.
     */
    private fun fetchBookingDetails() {
        bookingId?.let { id ->
            database.child("Bookings").child(id)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val booking = snapshot.getValue(BookingModel::class.java)
                        // HOW null safety handled:
                        // Using safe call ?. and checking if snapshot exists to prevent crashes.
                        if (booking != null) {
                            mapBookingData(booking)
                            // WHY Firebase fetch happens from multiple nodes:
                            // Bookings node only contains IDs (userId, salonId).
                            // To show names and phone numbers, we must fetch from Users and Salons nodes.
                            fetchCustomerDetails(booking.userId)
                            fetchSalonDetails(booking.salonId)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ReceiptActivity, "Failed to load booking", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    /**
     * Fetches customer specific info (Name, Phone) from Users node.
     */
    private fun fetchCustomerDetails(userId: String) {
        database.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value?.toString() ?: "N/A"
                    val phone = snapshot.child("phone").value?.toString() ?: "N/A"
                    binding.tvCustomerName.text = name
                    binding.tvCustomerPhone.text = phone
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * Fetches salon specific info (Name) from Salons node.
     */
    private fun fetchSalonDetails(salonId: String) {
        database.child("Salons").child(salonId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val salonName = snapshot.child("salonName").value?.toString() ?: "N/A"
                    binding.tvSalonName.text = salonName
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * HOW receipt data mapping implemented:
     * We map the BookingModel fields to the UI components.
     * Currency symbols and labels are added for better readability.
     */
    private fun mapBookingData(booking: BookingModel) {
        binding.tvBookingId.text = "ID: #${booking.bookingId}"
        binding.tvBookingDateTime.text = "${booking.bookingDate} at ${booking.bookingTime}"
        binding.tvServicesList.text = booking.services
        binding.tvSubtotal.text = "₹${booking.subtotal ?: 0}"
        binding.tvDiscount.text = "-₹${booking.discount ?: 0}"
        binding.tvGrandTotal.text = "₹${booking.grandTotal ?: 0}"
    }

    /**
     * WHY QR contains bookingId:
     * The QR code encodes the bookingId so it can be scanned by the salon owner
     * to quickly verify the booking in their system.
     */
    private fun generateQRCode(content: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)
            binding.ivQRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
