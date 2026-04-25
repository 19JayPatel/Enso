package com.example.enso.customer

import com.example.enso.owner.ServiceModel

/**
 * Singleton object to manage booking session data temporarily.
 */
object BookingSessionManager {
    var bookingDate: String = ""
    var bookingTime: String = ""
    var salonId: String = ""
    var ownerId: String = ""
    var salonName: String = ""
    var salonLocation: String = ""
    var salonImageUrl: String = ""
    var serviceId: String = ""
    var serviceName: String = ""
    var totalDurationMinutes: Int = 0
    var servicePrice: String = ""
    var currentUserId: String = ""
    var customerName: String = ""

    var selectedServices: MutableList<ServiceModel> = mutableListOf()

    /**
     * Calculates the combined duration of all selected services.
     */
    fun calculateTotalDuration(selectedServices: List<ServiceModel>) {
        totalDurationMinutes = 0
        for (service in selectedServices) {
            totalDurationMinutes += service.duration?.toIntOrNull() ?: 0
        }
    }

    /**
     * Resets all fields in the session manager.
     */
    fun reset() {
        bookingDate = ""
        bookingTime = ""
        salonId = ""
        ownerId = ""
        salonName = ""
        salonLocation = ""
        salonImageUrl = ""
        serviceId = ""
        serviceName = ""
        totalDurationMinutes = 0
        servicePrice = ""
        currentUserId = ""
        customerName = ""
        selectedServices.clear()
    }
}
