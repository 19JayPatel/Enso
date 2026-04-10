package com.example.enso.owner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R

/**
 * Salon Owner Services Management Fragment
 * This fragment allows owners to view and toggle availability of their services.
 * Features: Search bar, Category filter, and a Toggleable list of services.
 */
class SalonOwnerServicesFragment : Fragment() {

    private lateinit var rvServices: RecyclerView
    private lateinit var adapter: ServicesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_salon_owner_services, container, false)

        // 1. Setup RecyclerView
        rvServices = view.findViewById(R.id.rvServices)
        rvServices.layoutManager = LinearLayoutManager(requireContext())

        // 2. Load Dummy Data
        val servicesList = listOf(
            Service("Hair Cut", "30 min • 2 stylists", "$25", true, "HC"),
            Service("Hair Styling", "45 min • 3 stylists", "$35", true, "HS"),
            Service("Hair Coloring", "90 min • 1 stylist", "$80", true, "HC"),
            Service("Nail Art", "60 min • 2 stylists", "$40", false, "NA"),
            Service("Hair Wash", "20 min • All stylists", "$15", true, "HW"),
            Service("Facial Treatment", "75 min • 1 specialist", "$65", true, "FT")
        )

        // 3. Initialize Adapter
        adapter = ServicesAdapter(servicesList)
        rvServices.adapter = adapter

        return view
    }
}

/**
 * Service Data Model
 */
data class Service(
    val name: String,
    val duration: String,
    val price: String,
    var isEnabled: Boolean,
    val initials: String
)

/**
 * Simple RecyclerView Adapter for Services
 * Beginner-friendly implementation without complex patterns.
 */
class ServicesAdapter(private val services: List<Service>) :
    RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvServiceDuration: TextView = view.findViewById(R.id.tvServiceDuration)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val switchEnable: SwitchCompat = view.findViewById(R.id.switchEnable)
        val cvInitials: CardView = view.findViewById(R.id.cvInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_owner, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = services[position]

        // Bind data to views
        holder.tvInitials.text = item.initials
        holder.tvServiceName.text = item.name
        holder.tvServiceDuration.text = item.duration
        holder.tvPrice.text = item.price
        holder.switchEnable.isChecked = item.isEnabled

        // Update model when switch toggled
        holder.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            item.isEnabled = isChecked
        }

        // Apply distinct colors to the initials circle for better UI
        val bgColors = listOf("#FDEEE7", "#E7F3FD", "#E7FDF0", "#FDF5E7")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32", "#EF6C00")
        val colorIdx = position % bgColors.size
        
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIdx]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIdx]))
    }

    override fun getItemCount() = services.size
}
