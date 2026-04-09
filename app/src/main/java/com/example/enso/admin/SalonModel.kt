package com.example.enso.admin

data class SalonModel(
    val salonId: String? = null,
    val salonName: String? = null,
    val ownerFirstName: String? = null,
    val ownerLastName: String? = null,
    val status: String? = "Pending",
    val bookings: Int? = 0,
    val services: Int? = 0,
    val rating: Double? = 0.0
)