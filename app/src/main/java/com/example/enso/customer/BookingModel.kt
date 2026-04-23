package com.example.enso.customer

/**
 * Model class for Booking data to be saved in Firebase.
 * This model defines the structure of each booking stored in the database.
 */
data class BookingModel(
    val bookingId: String = "",
    val userId: String = "",
    val salonId: String = "",
    val salonName: String = "",
    val services: String = "",
    
    // WHY:
    //
    // RecyclerView reuses item views while scrolling.
    // If adapter fetches salon data separately from Firebase,
    // scrolling becomes slower and increases database reads.
    //
    // Storing salon snapshot data inside BookingModel ensures:
    //
    // faster RecyclerView rendering
    // fewer Firebase reads
    // smoother scrolling performance
    var salonLocation: String = "",
    var salonImageUrl: String = "",

    // This variable stores the sum of all selected services before any discount
    // It is saved in Firebase so we can track the original price of services
    val subtotal: Int? = 0,

    // This variable stores the discount amount (default is 0)
    // It is saved in Firebase so coupon system can be added later
    val discount: Int? = 0,

    // This variable stores the final amount after subtracting discount from subtotal
    // This is the actual amount the customer will pay
    val grandTotal: Int? = 0,

    val bookingDate: String = "",
    val bookingTime: String = "",
    val paymentMethod: String? = "",
    val paymentStatus: String? = "",
    val status: String = "confirmed",
    val createdAt: Long = System.currentTimeMillis()
)
