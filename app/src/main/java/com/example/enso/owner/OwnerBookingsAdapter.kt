package com.example.enso.owner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.BookingModel
import com.google.firebase.database.FirebaseDatabase

/**
 * Adapter for Salon Owner to view and manage bookings.
 * WHY: This adapter connects the BookingModel data from Firebase to the item_booking_owner layout.
 */
class OwnerBookingsAdapter(
    private var bookingList: List<BookingModel>,
    private val highlightBookingId: String? = null
) : RecyclerView.Adapter<OwnerBookingsAdapter.BookingViewHolder>() {

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_owner, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        holder.tvDateHeader.text = booking.bookingDate
        
        // Use pre-saved customer name for better performance
        val name = booking.customerName.ifEmpty { "Customer" }
        holder.tvCustomerName.text = name
        holder.tvInitials.text = name.take(1).uppercase()

        holder.tvServiceName.text = booking.serviceName
        holder.tvBookingTime.text = booking.bookingTime
        holder.tvBookingPrice.text = "$${booking.price}"
        holder.tvStatusBadge.text = booking.status.replaceFirstChar { it.uppercase() }

        if (booking.bookingId == highlightBookingId) {
            applyBubbleHighlight(holder)
        } else {
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
        }

        // BUTTON: MARK COMPLETE
        holder.btnMarkComplete.setOnClickListener {
            if (booking.status == "upcoming") {
                FirebaseDatabase.getInstance().getReference("Bookings").child(booking.bookingId)
                    .child("status").setValue("completed")
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Booking marked as Completed", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(holder.itemView.context, "Only 'Upcoming' bookings can be completed", Toast.LENGTH_SHORT).show()
            }
        }

        // BUTTON: CANCEL
        holder.btnCancel.setOnClickListener {
            if (booking.status == "upcoming") {
                FirebaseDatabase.getInstance().getReference("Bookings").child(booking.bookingId)
                    .child("status").setValue("cancelled")
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(holder.itemView.context, "Only 'Upcoming' bookings can be cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyBubbleHighlight(holder: BookingViewHolder) {
        holder.itemView.scaleX = 0.9f
        holder.itemView.scaleY = 0.9f
        holder.itemView.animate()
            .scaleX(1.05f).scaleY(1.05f)
            .setDuration(250)
            .withEndAction {
                holder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }.start()
    }

    override fun getItemCount(): Int = bookingList.size

    fun updateList(newList: List<BookingModel>) {
        this.bookingList = newList
        notifyDataSetChanged()
    }
}
