package com.example.enso.customer.models

/**
 * Model class for Booking data to be saved in Firebase.
 * Updated to match required structure for complete booking flow.
 */
data class BookingModel(
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val salonId: String = "",
    val ownerId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val salonName: String = "",
    val salonImageUrl: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val price: String = "",
    val duration: String = "",
    val status: String = "upcoming",
    val createdAt: Long = System.currentTimeMillis()
)