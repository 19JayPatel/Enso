package com.example.enso.customer

import com.example.enso.owner.ServiceModel

/**
 * Singleton object to manage booking session data temporarily.
 */
object BookingSessionManager {
    // Variable to store selected booking date in "dd MMM yyyy" format
    var bookingDate: String = ""
    
    // Variable to store selected booking time in "hh:mm a" format
    var bookingTime: String = ""
    
    // Variable to store selected salon ID
    var salonId: String = ""

    // Variable to store selected salon Name
    var salonName: String = ""

    // WHY:
    // Storing salon location and image URL in the session manager ensures
    // that this data is available throughout the booking flow without
    // having to repeatedly pass it through intents or re-query Firebase.
    var salonLocation: String = ""
    var salonImageUrl: String = ""

    // Variable to store selected service ID
    var serviceId: String = ""

    // Variable to store selected service Name
    var serviceName: String = ""

    // Variable to store total duration of all selected services
    var totalDurationMinutes: Int = 0

    // Variable to store service price
    var servicePrice: String = ""

    // Variable to store current user ID
    var currentUserId: String = ""

    // ✅ ADD THIS
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
        salonName = ""
        salonLocation = ""
        salonImageUrl = ""
        serviceId = ""
        serviceName = ""
        totalDurationMinutes = 0
        servicePrice = ""
        currentUserId = ""
        selectedServices.clear()
    }
}
