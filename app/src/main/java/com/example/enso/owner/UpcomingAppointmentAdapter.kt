package com.example.enso.owner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.BookingModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Adapter for displaying bookings in the Salon Owner panel.
 * Reused and updated to work with the standard BookingModel from Firebase.
 */
class UpcomingAppointmentAdapter(
    private val bookingList: MutableList<BookingModel>,
    private val listener: OnViewClickListener
) : RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder>() {

    interface OnViewClickListener {
        fun onViewClicked(bookingId: String)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvService: TextView = view.findViewById(R.id.tvService)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cvStatus: CardView = view.findViewById(R.id.cvStatus)
        val cvInitials: CardView = view.findViewById(R.id.cvInitials)
        val btnView: CardView = view.findViewById(R.id.btnView)
    }

    fun updateList(newList: List<BookingModel>) {
        bookingList.clear()
        bookingList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = bookingList[position]

        // FIX: Use customerId instead of userId
        holder.tvName.text = appointment.customerName.ifEmpty { "Loading..." }
        
        if (appointment.customerName.isEmpty()) {
            FirebaseDatabase.getInstance().reference
                .child("Users")
                .child(appointment.customerId)
                .child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.getValue(String::class.java) ?: "Unknown Customer"
                        holder.tvName.text = name
                        holder.tvInitials.text = name.take(1).uppercase()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        holder.tvName.text = "Unknown Customer"
                        holder.tvInitials.text = "U"
                    }
                })
        } else {
            holder.tvInitials.text = appointment.customerName.take(1).uppercase()
        }
        
        // FIX: Use serviceName and price from BookingModel
        holder.tvService.text = appointment.serviceName
        holder.tvPrice.text = "₹${appointment.price}"
        holder.tvTime.text = appointment.bookingTime
        holder.tvStatus.text = appointment.status.replaceFirstChar { it.uppercase() }

        if (appointment.status.equals("confirmed", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#E7FDF0"))
        } else if (appointment.status.equals("pending", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"))
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FDF5E7"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#8E8E8E"))
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
        }
        
        val bgColors = listOf("#F4E8E1", "#E7F3FD", "#E7FDF0")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32")
        val colorIndex = position % bgColors.size
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIndex]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIndex]))

        holder.btnView.setOnClickListener {
            listener.onViewClicked(appointment.bookingId)
        }
    }

    override fun getItemCount() = bookingList.size
}
