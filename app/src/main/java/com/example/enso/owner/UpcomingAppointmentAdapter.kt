package com.example.enso.owner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R

data class UpcomingAppointment(
    val name: String,
    val service: String,
    val price: String,
    val time: String,
    val status: String, // "Confirmed" or "Pending"
    val initials: String
)

class UpcomingAppointmentAdapter(private val appointments: List<UpcomingAppointment>) :
    RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvService: TextView = view.findViewById(R.id.tvService)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cvStatus: CardView = view.findViewById(R.id.cvStatus)
        val cvInitials: CardView = view.findViewById(R.id.cvInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.tvInitials.text = appointment.initials
        holder.tvName.text = appointment.name
        holder.tvService.text = appointment.service
        holder.tvPrice.text = appointment.price
        holder.tvTime.text = appointment.time
        holder.tvStatus.text = appointment.status

        if (appointment.status == "Confirmed") {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#E7FDF0"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"))
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FDF5E7"))
        }
        
        // Color based on initials/index for variety
        val bgColors = listOf("#F4E8E1", "#E7F3FD", "#E7FDF0")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32")
        val colorIndex = position % bgColors.size
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIndex]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIndex]))
    }

    override fun getItemCount() = appointments.size
}
