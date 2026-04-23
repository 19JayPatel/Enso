package com.example.enso.customer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.enso.R
import com.example.enso.customer.BookingModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Adapter for displaying customer bookings in a RecyclerView.
 * This adapter is used for all three sections: Upcoming, Completed, and Cancelled.
 */
class CustomerBookingsAdapter(
    private var bookings: List<BookingModel>,
    private val onCancelClick: (BookingModel) -> Unit,
    private val onViewReceiptClick: (BookingModel) -> Unit
) : RecyclerView.Adapter<CustomerBookingsAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // WHY:
        // RecyclerView adapter controls card UI binding dynamically.
        // If references missing, XML fallback values appear instead of Firebase data.
        val tvBookingDateTime = view.findViewById<TextView>(R.id.tvBookingDateTime)
        val tvSalonName = view.findViewById<TextView>(R.id.tvSalonName)
        val tvSalonLocation = view.findViewById<TextView>(R.id.tvSalonLocation)
        val tvServices = view.findViewById<TextView>(R.id.tvServices)
        val tvStatusBadge = view.findViewById<TextView>(R.id.tvStatusBadge)
        val ivSalonImage = view.findViewById<ShapeableImageView>(R.id.ivSalonImage)
        val cvSalonInitials = view.findViewById<MaterialCardView>(R.id.cvSalonInitials)
        val tvSalonInitials = view.findViewById<TextView>(R.id.tvSalonInitials)
        val btnCancelBooking = view.findViewById<MaterialButton>(R.id.btnCancelBooking)
        val btnViewReceipt = view.findViewById<MaterialButton>(R.id.btnViewReceipt)
        val llActionButtons = view.findViewById<LinearLayout>(R.id.llActionButtons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_customer, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // WHY RecyclerView replaces static cards: 
        // Dynamic lists allow for a flexible number of items loaded from a database, 
        // whereas static cards are hardcoded and cannot scale with user data.

        holder.tvBookingDateTime.text = "${booking.bookingDate} - ${booking.bookingTime}"
        holder.tvSalonName.text = booking.salonName
        holder.tvServices.text = "Services: ${booking.services}"

        // --- STEP 1: INITIALS FALLBACK LOGIC ---

        // WHY initials logic used:
        //
        // Enso currently does NOT store salon images for every salon.
        // So initials are shown as fallback avatar.
        //
        // Example:
        //
        // "Urban Style" → US
        // "Hair Studio" → HS

        val initials = booking.salonName
            .split(" ")
            .mapNotNull {
                // HOW initials extracted:
                //
                // Take first character of each word safely
                it.firstOrNull()?.uppercase()
            }
            .take(2)
            // WHY only first two letters:
            //
            // UI avatar circle supports max 2 letters
            .joinToString("")

        holder.tvSalonInitials.text = if (initials.isNotEmpty()) initials else "?"

        // --- STEP 2: IMAGE VS INITIALS VISIBILITY ---

        // WHY:
        // Glide is optimized for RecyclerView image loading.
        // Provides caching + smooth scrolling performance.
        if (booking.salonImageUrl.isNotEmpty()) {
            holder.ivSalonImage.visibility = View.VISIBLE
            holder.cvSalonInitials.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(booking.salonImageUrl)
                .placeholder(R.drawable.hair_avenue)
                .into(holder.ivSalonImage)
        } else {
            // WHY fallback UI used:
            // If image is missing, we hide ImageView and show Initials Card.
            holder.ivSalonImage.visibility = View.GONE
            holder.cvSalonInitials.visibility = View.VISIBLE
        }

        // --- STEP 3: DYNAMIC LOCATION FETCH ---

        // WHY:
        // Location belongs to: Salons node.
        // This keeps Firebase structure normalized and clean without duplicating 
        // data inside BookingModel.

        val salonsRef = FirebaseDatabase.getInstance().getReference("Salons")

        salonsRef.child(booking.salonId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // WHY snapshot check required:
                    // Prevent crash if salon deleted or missing
                    if (snapshot.exists()) {
                        // Handle nested address map if present, or direct location field
                        val addressSnap = snapshot.child("address")
                        val location = if (addressSnap.exists()) {
                            val city = addressSnap.child("city").value?.toString() ?: ""
                            val country = addressSnap.child("country").value?.toString() ?: ""
                            if (city.isNotEmpty() && country.isNotEmpty()) "$city, $country" else country
                        } else {
                            snapshot.child("location").value?.toString()
                        }

                        holder.tvSalonLocation.text = location ?: "Location unavailable"
                    } else {
                        holder.tvSalonLocation.text = "Location unavailable"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // WHY fallback message used:
                    // Prevent blank UI if Firebase fails
                    holder.tvSalonLocation.text = "Location unavailable"
                }
            })

        // WHY:
        // Status badge improves booking readability.
        // Users instantly understand booking state visually.
        when (booking.status.lowercase()) {
            "upcoming", "confirmed" -> {
                holder.tvStatusBadge.visibility = View.VISIBLE
                holder.tvStatusBadge.text = "Upcoming"
                holder.tvStatusBadge.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.primary_brown
                    )
                )
            }
            "completed" -> {
                holder.tvStatusBadge.visibility = View.VISIBLE
                holder.tvStatusBadge.text = "Completed"
                holder.tvStatusBadge.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.success_green
                    )
                )
            }
            "cancelled" -> {
                holder.tvStatusBadge.visibility = View.VISIBLE
                holder.tvStatusBadge.text = "Cancelled"
                holder.tvStatusBadge.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.error_red
                    )
                )
            }
            else -> {
                holder.tvStatusBadge.visibility = View.GONE
            }
        }

        // Handle visibility of action buttons based on status
        // HOW booking status separation works:
        // We only show "Cancel Booking" for "Upcoming" appointments.
        if (booking.status.equals("Upcoming", ignoreCase = true) || booking.status.equals("confirmed", ignoreCase = true)) {
            holder.llActionButtons.visibility = View.VISIBLE
            holder.btnCancelBooking.visibility = View.VISIBLE
        } else if (booking.status.equals("Completed", ignoreCase = true)) {
            holder.llActionButtons.visibility = View.VISIBLE
            holder.btnCancelBooking.visibility = View.GONE
        } else {
            // Cancelled
            holder.llActionButtons.visibility = View.GONE
        }

        holder.btnCancelBooking.setOnClickListener {
            onCancelClick(booking)
        }

        holder.btnViewReceipt.setOnClickListener {
            onViewReceiptClick(booking)
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<BookingModel>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
}
