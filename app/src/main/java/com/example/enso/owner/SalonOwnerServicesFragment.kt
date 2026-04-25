package com.example.enso.owner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Salon Owner Services Management Fragment
 * Displays dynamic services from Firebase for the logged-in owner.
 */
class SalonOwnerServicesFragment : Fragment() {

    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServicesAdapter
    private lateinit var btnAddService: Button
    private lateinit var database: DatabaseReference
    private val serviceList = ArrayList<ServiceModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_services, container, false)

        // 1. Firebase Check
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return view
        }
        val userId = user.uid

        // 2. Setup RecyclerView
        rvServices = view.findViewById(R.id.rvServices)
        rvServices.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServicesAdapter(serviceList)
        rvServices.adapter = serviceAdapter

        // 3. Fetch Data from Firebase
        fetchOwnerServices(userId)

        // 4. Navigation: Open AddNewServiceActivity
        btnAddService = view.findViewById(R.id.btnAddService)
        btnAddService.setOnClickListener {
            val intent = Intent(requireContext(), AddNewServiceActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun fetchOwnerServices(userId: String) {
        // BUG 2 FIX: Listen directly to the owner's services path for instant updates
        database = FirebaseDatabase.getInstance().getReference("Services").child(userId)
        
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceList.clear()
                if (snapshot.exists()) {
                    for (serviceSnap in snapshot.children) {
                        val service = serviceSnap.getValue(ServiceModel::class.java)
                        if (service != null) {
                            serviceList.add(service)
                        }
                    }
                }
                
                if (serviceList.isEmpty()) {
                    // Only show toast if no data exists
                    if (isAdded) {
                        // Toast.makeText(requireContext(), "No services added yet", Toast.LENGTH_SHORT).show()
                    }
                }
                
                serviceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}

/**
 * Simple RecyclerView Adapter for Firebase Services
 */
class ServicesAdapter(private val services: List<ServiceModel>) :
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
        val model = services[position]

        // Bind data to views
        holder.tvServiceName.text = model.serviceName
        holder.tvPrice.text = "$" + model.price
        holder.tvServiceDuration.text = model.duration + " min"
        
        // Status Toggle
        holder.switchEnable.isChecked = model.status == "active"

        // Set Initials based on Category (Step 7)
        holder.tvInitials.text = when(model.category) {
            "Hair" -> "H"
            "Nails" -> "N"
            "Skin Care" -> "S"
            "Spa & Wellness" -> "SP"
            else -> model.serviceName?.take(1)?.uppercase() ?: "S"
        }

        // Apply distinct colors to the initials circle for better UI
        val bgColors = listOf("#FDEEE7", "#E7F3FD", "#E7FDF0", "#FDF5E7")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32", "#EF6C00")
        val colorIdx = position % bgColors.size
        
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIdx]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIdx]))
        
        // Handle Toggle
        holder.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "active" else "inactive"
            if (model.status != newStatus) {
                model.status = newStatus
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null && model.serviceId != null) {
                    FirebaseDatabase.getInstance().getReference("Services")
                        .child(userId)
                        .child(model.serviceId!!)
                        .child("status")
                        .setValue(newStatus)
                }
            }
        }
    }

    override fun getItemCount() = services.size
}
