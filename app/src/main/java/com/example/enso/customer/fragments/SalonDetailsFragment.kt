package com.example.enso.customer.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.BookingSessionManager
import com.example.enso.customer.activities.DateTimeActivity
import com.example.enso.owner.models.ServiceModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Updated Service model as requested
 */
data class Service(
    var serviceId: String = "",
    var serviceName: String = "",
    var price: String = "",
    var duration: String = "",
    var ownerId: String = "",
    var category: String = "",
    var description: String = ""
)

class SalonDetailsFragment : Fragment() {

    private val selectedServices = mutableSetOf<String>()
    private lateinit var btnContinue: MaterialButton
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = ArrayList<Service>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_salon_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get Salon Data from Arguments
        val name = arguments?.getString("name") ?: "Hair Avenue"
        val location = arguments?.getString("location") ?: "Location"
        val rating = arguments?.getString("rating") ?: "4.7 (312)"
        val salonId = arguments?.getString("salonId")
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        // 2. References to UI Views
        val tvInitials = view.findViewById<TextView>(R.id.tvInitials)
        val tvHeaderSalonName = view.findViewById<TextView>(R.id.tvHeaderSalonName)
        val tvSalonName = view.findViewById<TextView>(R.id.tvSalonName)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvRating = view.findViewById<TextView>(R.id.tvRating)
        btnContinue = view.findViewById(R.id.btnContinue)
        rvServices = view.findViewById(R.id.rvServices)

        // 3. Setup RecyclerView
        rvServices.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServiceAdapter(serviceList) { service, isSelected ->
            if (isSelected) {
                selectedServices.add(service.serviceId)
            } else {
                selectedServices.remove(service.serviceId)
            }
            updateContinueButton()
        }
        rvServices.adapter = serviceAdapter

        // 4. Generate Initials
        val initials = name.split(" ").filter { it.isNotEmpty() }.map { it[0] }.joinToString("").take(2).uppercase()

        // 5. Set Static Data to UI
        tvInitials.text = initials
        tvHeaderSalonName.text = name
        tvSalonName.text = name
        tvLocation.text = location
        tvRating.text = rating

        // 6. 🔥 FETCH SERVICES BASED ON SALON OWNER ID
        if (salonId != null) {
            fetchSalonOwnerAndServices(salonId)
        }

        // Fetch current user name for booking
        fetchCurrentUserName()

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

        btnContinue.setOnClickListener {
            // STEP 1: Pass dynamic values using Bundle/Intent
            val selectedServiceObjects = serviceList.filter { selectedServices.contains(it.serviceId) }

            val intent = Intent(requireContext(), DateTimeActivity::class.java)
            intent.putExtra("salonId", salonId)
            intent.putExtra("salonName", name)
            intent.putExtra("salonAddress", location)
            intent.putExtra("salonRating", rating)
            intent.putExtra("salonDistance", "2 km")

            val total = selectedServiceObjects.sumOf { it.price.replace("$", "").toDoubleOrNull() ?: 0.0 }
            intent.putExtra("totalPrice", "$${String.format("%.2f", total)}")
            intent.putExtra("discount", "$0.00")

            val totalDuration = selectedServiceObjects.sumOf { it.duration.toIntOrNull() ?: 0 }
            intent.putExtra("serviceDuration", "$totalDuration mins")

            // Calculate and store total duration in BookingSessionManager
            val selectedServiceModels = selectedServiceObjects.map {
                ServiceModel(
                    serviceId = it.serviceId,
                    serviceName = it.serviceName,
                    price = it.price,
                    duration = it.duration,
                    ownerId = it.ownerId,
                    category = it.category
                )
            }
            BookingSessionManager.selectedServices = selectedServiceModels.toMutableList()
            BookingSessionManager.salonId = salonId ?: ""
            BookingSessionManager.salonName = name

            BookingSessionManager.calculateTotalDuration(selectedServiceModels)

            BookingSessionManager.salonLocation = location
            BookingSessionManager.salonImageUrl = imageUrl

            startActivity(intent)
        }

        updateContinueButton()
    }

    private fun fetchCurrentUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    BookingSessionManager.customerName = snapshot.getValue(String::class.java) ?: "Customer"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * BUG FIX:
     * 1. Fetch Salon data to get the ownerId
     * 2. Then fetch Services using that ownerId from "Services/ownerId"
     */
    private fun fetchSalonOwnerAndServices(salonId: String) {
        val salonRef = FirebaseDatabase.getInstance().getReference("Salons").child(salonId)
        
        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val ownerId = snapshot.child("ownerId").getValue(String::class.java)
                    if (!ownerId.isNullOrEmpty()) {
                        BookingSessionManager.ownerId = ownerId
                        loadServicesByOwner(ownerId)
                    } else {
                        Toast.makeText(requireContext(), "Owner ID not found for this salon", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching salon details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadServicesByOwner(ownerId: String) {
        val serviceRef = FirebaseDatabase.getInstance().getReference("Services").child(ownerId)
        
        serviceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceList.clear()
                for (data in snapshot.children) {
                    val service = data.getValue(Service::class.java)
                    if (service != null) {
                        service.serviceId = data.key ?: ""
                        serviceList.add(service)
                    }
                }
                serviceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load services", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateContinueButton() {
        val count = selectedServices.size
        btnContinue.text = "Continue ($count)"

        if (count > 0) {
            btnContinue.isEnabled = true
            btnContinue.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.primary_brown)
            )
            btnContinue.setTextColor(Color.WHITE)
        } else {
            btnContinue.isEnabled = false
            btnContinue.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D3D3D3"))
            btnContinue.setTextColor(Color.parseColor("#8E8E8E"))
        }
    }

    /**
     * Inner Adapter for Services
     */
    inner class ServiceAdapter(
        private val services: List<Service>,
        private val onServiceClick: (Service, Boolean) -> Unit
    ) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

        private val selectedItems = mutableSetOf<Int>()

        inner class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cvService: MaterialCardView = view.findViewById(R.id.cvService)
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
            val tvDuration: TextView = view.findViewById(R.id.tvDuration)
            val cbService: MaterialCheckBox = view.findViewById(R.id.cbService)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_customer, parent, false)
            return ServiceViewHolder(view)
        }

        override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
            val service = services[position]
            holder.tvName.text = service.serviceName
            
            // Format price and duration to match image ($35.00)
            val formattedPrice = if (service.price.contains("$")) service.price else "$${service.price}"
            val finalPrice = if (formattedPrice.contains(".")) formattedPrice else "$formattedPrice.00"
            
            holder.tvPrice.text = finalPrice
            holder.tvDuration.text = "${service.duration} Mins"
            
            val isSelected = selectedItems.contains(position)
            holder.cvService.isChecked = isSelected
            holder.cbService.isChecked = isSelected

            if (isSelected) {
                holder.cvService.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_brown)))
                holder.cvService.setStrokeWidth(3)
            } else {
                holder.cvService.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT))
                holder.cvService.setStrokeWidth(0)
            }

            holder.cvService.setOnClickListener {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(position)
                    onServiceClick(service, false)
                } else {
                    selectedItems.add(position)
                    onServiceClick(service, true)
                }
                notifyItemChanged(position)
            }
        }

        override fun getItemCount(): Int = services.size
    }
}
