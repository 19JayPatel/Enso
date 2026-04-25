package com.example.enso.admin

data class SalonModel(
    var salonId: String = "",
    var salonName: String = "",
    var ownerId: String = "",
    var ownerName: String = "",
    var status: String = "",
    var bookingsCount: Int = 0,
    var servicesCount: Int = 0,
    var rating: Double = 4.7
)