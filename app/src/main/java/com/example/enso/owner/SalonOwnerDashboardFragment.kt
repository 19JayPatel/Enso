package com.example.enso.owner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SalonOwnerDashboardFragment : Fragment() {

    private lateinit var rvAppointments: RecyclerView
    private lateinit var adapter: DashboardAppointmentAdapter
    private lateinit var tvSalonName: TextView
    private lateinit var tvLocation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_dashboard, container, false)

        // ✅ STEP 1: GET CURRENT USER
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return view
        }

        val userId = user.uid

        // Initialize UI Elements
        tvSalonName = view.findViewById(R.id.tvSalonName)
        tvLocation = view.findViewById(R.id.tvLocation)
        rvAppointments = view.findViewById(R.id.rvDashboardAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(requireContext())

        // ✅ STEP 2: FETCH SALON USING ownerId (from "Salons" node)
        val salonRef = FirebaseDatabase.getInstance().getReference("Salons")

        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (salonSnap in snapshot.children) {
                        val ownerId = salonSnap.child("ownerId").getValue(String::class.java)

                        // ✅ MATCH LOGGED IN USER
                        if (ownerId == userId) {
                            val salonName = salonSnap.child("salonName").getValue(String::class.java)

                            // address is a child of salonSnap
                            val street = salonSnap.child("address").child("street").getValue(String::class.java)
                            val state = salonSnap.child("address").child("state").getValue(String::class.java)
                            val country = salonSnap.child("address").child("country").getValue(String::class.java)

                            val location = "$street, $state, $country"

                            // SET UI
                            tvSalonName.text = salonName ?: "Salon Name"
                            tvLocation.text = if (!street.isNullOrEmpty()) location else "Location not set"

                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })

        // Setup Static Appointments List (Kept from original)
        val appointmentsList = listOf(
            DashboardAppointment("Sarah Johnson", "Hair Cut + Hair Wash", "9:30 AM", "$45.00", "Confirmed", "SJ"),
            DashboardAppointment("Mike Rodriguez", "Hair Styling", "11:00 AM", "$30.00", "Pending", "MR"),
            DashboardAppointment("Aisha Patel", "Nail Art + Manicure", "2:15 PM", "$55.00", "Confirmed", "AP")
        )
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
 * RecyclerView Adapter for appointments
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
        holder.tvInitials.text = item.initials
        holder.tvName.text = item.name
        holder.tvService.text = item.service
        holder.tvPrice.text = item.price
        holder.tvTime.text = item.time
        holder.tvStatus.text = item.status

        if (item.status == "Confirmed") {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) 
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#E7FDF0"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00")) 
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FDF5E7"))
        }

        val bgColors = listOf("#F4E8E1", "#E7F3FD", "#E7FDF0")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32")
        val colorIdx = position % bgColors.size
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIdx]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIdx]))
    }

    override fun getItemCount() = appointments.size
}
