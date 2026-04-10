package com.example.enso.owner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R

/**
 * Salon Owner Dashboard Fragment
 * This fragment displays the salon owner's overview: revenue, stats, and upcoming appointments.
 * Built with simple Kotlin and XML as requested.
 */
class SalonOwnerDashboardFragment : Fragment() {

    private lateinit var rvAppointments: RecyclerView
    private lateinit var adapter: DashboardAppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_salon_owner_dashboard, container, false)

        // 1. Initialize RecyclerView
        rvAppointments = view.findViewById(R.id.rvDashboardAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(requireContext())

        // 2. Prepare Dummy Data (Static List)
        val appointmentsList = listOf(
            DashboardAppointment("Sarah Johnson", "Hair Cut + Hair Wash", "9:30 AM", "$45.00", "Confirmed", "SJ"),
            DashboardAppointment("Mike Rodriguez", "Hair Styling", "11:00 AM", "$30.00", "Pending", "MR"),
            DashboardAppointment("Aisha Patel", "Nail Art + Manicure", "2:15 PM", "$55.00", "Confirmed", "AP")
        )

        // 3. Setup Adapter with the dummy data
        adapter = DashboardAppointmentAdapter(appointmentsList)
        rvAppointments.adapter = adapter

        return view
    }
}

/**
 * Simple Data Class for Appointment
 */
data class DashboardAppointment(
    val name: String,
    val service: String,
    val time: String,
    val price: String,
    val status: String,
    val initials: String
)

/**
 * RecyclerView Adapter for the upcoming appointments list
 */
class DashboardAppointmentAdapter(private val appointments: List<DashboardAppointment>) :
    RecyclerView.Adapter<DashboardAppointmentAdapter.ViewHolder>() {

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
        val item = appointments[position]

        // Set basic info
        holder.tvInitials.text = item.initials
        holder.tvName.text = item.name
        holder.tvService.text = item.service
        holder.tvPrice.text = item.price
        holder.tvTime.text = item.time
        holder.tvStatus.text = item.status

        // Dynamic Status Styling (Confirmed = Green, Pending = Orange)
        if (item.status == "Confirmed") {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) 
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#E7FDF0"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00")) 
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FDF5E7"))
        }

        // Initials Avatar Colors (Cycle through a few pleasant colors)
        val bgColors = listOf("#F4E8E1", "#E7F3FD", "#E7FDF0")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32")
        val colorIdx = position % bgColors.size
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIdx]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIdx]))
    }

    override fun getItemCount() = appointments.size
}
