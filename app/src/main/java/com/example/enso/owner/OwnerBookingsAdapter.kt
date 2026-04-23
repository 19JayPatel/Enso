package com.example.enso.owner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.BookingModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Adapter for Salon Owner to view and manage bookings.
 * WHY: This adapter connects the BookingModel data from Firebase to the item_booking_owner layout.
 */
class OwnerBookingsAdapter(
    private var bookingList: List<BookingModel>,
    // STEP 3:
    // WHAT: add parameter highlightBookingId
    // WHY: Allows adapter to detect selected booking during binding.
    private val highlightBookingId: String? = null
) : RecyclerView.Adapter<OwnerBookingsAdapter.BookingViewHolder>() {

    /**
     * ViewHolder holds references to the UI elements for each booking item.
     * WHY: Using ViewHolder pattern improves performance by avoiding repeated findViewById calls.
     */
    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateHeader: TextView = view.findViewById(R.id.tvDateHeader)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvBookingTime: TextView = view.findViewById(R.id.tvBookingTime)
        val tvBookingPrice: TextView = view.findViewById(R.id.tvBookingPrice)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val btnMarkComplete: TextView = view.findViewById(R.id.btnMarkComplete)
        val btnCancel: TextView = view.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        // WHY: We inflate item_booking_owner.xml as requested to design the row layout.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_owner, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        // WHAT: Displaying booking date as a header for clarity.
        holder.tvDateHeader.text = booking.bookingDate
        
        // STEP 1, 2, 3, 4, 5:
        // WHAT: Fetch customer name using booking.userId
        // WHY: Replace unreadable Firebase UID with human-friendly display name
        // HOW: Read Users/{userId}/name once during ViewHolder binding using addListenerForSingleValueEvent.
        holder.tvCustomerName.text = "Loading..." // Temporary placeholder
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(booking.userId)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java) ?: "Unknown Customer"
                    holder.tvCustomerName.text = name
                    
                    // WHAT: Set initials based on the fetched name
                    // WHY: Visual consistency with other parts of the app
                    // HOW: Extract first letter of the name
                    holder.tvInitials.text = if (name != "Unknown Customer") {
                        name.take(1).uppercase()
                    } else {
                        "C"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.tvCustomerName.text = "Unknown Customer"
                    holder.tvInitials.text = "C"
                }
            })

        // WHAT: Filling in other booking details from the model.
        holder.tvServiceName.text = booking.services
        holder.tvBookingTime.text = booking.bookingTime
        holder.tvBookingPrice.text = "₹${booking.grandTotal}"
        holder.tvStatusBadge.text = booking.status.replaceFirstChar { it.uppercase() }

        // STEP 4:
        // WHAT: apply animation ONLY when booking matches highlightId
        // WHY: Prevents animation running on all items. Ensures only selected booking animates.
        // HOW: Call applyBubbleHighlight if IDs match.
        if (booking.bookingId == highlightBookingId) {
            applyBubbleHighlight(holder)
        } else {
            // Reset state for recycled views
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
        }

        // BUTTON: MARK COMPLETE
        holder.btnMarkComplete.setOnClickListener {
            if (booking.status == "confirmed") {
                val bookingRef = FirebaseDatabase.getInstance().getReference("Bookings").child(booking.bookingId)
                bookingRef.child("status").setValue("completed")
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Booking marked as Completed", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(holder.itemView.context, "Only 'Confirmed' bookings can be completed", Toast.LENGTH_SHORT).show()
            }
        }

        // BUTTON: CANCEL
        holder.btnCancel.setOnClickListener {
            if (booking.status == "confirmed") {
                val bookingRef = FirebaseDatabase.getInstance().getReference("Bookings").child(booking.bookingId)
                bookingRef.child("status").setValue("cancelled")
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(holder.itemView.context, "Only 'Confirmed' bookings can be cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // STEP 5:
    // WHAT: Create animation function applyBubbleHighlight
    // WHY: Creates smooth bubble pop highlight effect similar to premium booking apps.
    // HOW: Chain scale animations using animate().
    private fun applyBubbleHighlight(holder: BookingViewHolder) {
        holder.itemView.scaleX = 0.9f
        holder.itemView.scaleY = 0.9f

        holder.itemView.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(250)
            .withEndAction {
                holder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    override fun getItemCount(): Int = bookingList.size

    /**
     * Updates the current list of bookings and notifies the RecyclerView.
     * WHY: This method is required to refresh the UI after fetching data from Firebase or filtering.
     */
    fun updateList(newList: List<BookingModel>) {
        this.bookingList = newList
        notifyDataSetChanged()
    }
}
