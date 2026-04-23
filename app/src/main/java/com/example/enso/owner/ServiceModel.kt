package com.example.enso.owner

data class ServiceModel(
    var serviceId: String? = "",
    var salonId: String? = "",
    var serviceName: String? = "",
    var category: String? = "",
    var description: String? = "",
    var price: String? = "",
    var duration: String? = "",
    var status: String? = "active",
    var createdAt: Long? = 0L
)
