package com.example.enso.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.google.firebase.database.FirebaseDatabase

class SalonAdapter(private var salonList: List<SalonModel>) :
    RecyclerView.Adapter<SalonAdapter.SalonViewHolder>() {

    class SalonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tv_initials)
        val tvSalonName: TextView = view.findViewById(R.id.tv_salon_name)
        val tvOwnerName: TextView = view.findViewById(R.id.tv_owner_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val cardStatus: CardView = view.findViewById(R.id.card_status)
        val tvBookings: TextView = view.findViewById(R.id.tv_bookings_count)
        val tvServices: TextView = view.findViewById(R.id.tv_services_count)
        val tvRating: TextView = view.findViewById(R.id.tv_rating)
        
        val btnSuspend: TextView = view.findViewById(R.id.btn_suspend)
        val btnAccept: LinearLayout = view.findViewById(R.id.btn_accept)
        val btnReject: LinearLayout = view.findViewById(R.id.btn_reject)
        val btnRestore: LinearLayout = view.findViewById(R.id.btn_restore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_salon, parent, false)
        return SalonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalonViewHolder, position: Int) {
        val salon = salonList[position]
        
        holder.tvSalonName.text = salon.salonName
        holder.tvOwnerName.text = "Owner: ${salon.ownerFirstName} ${salon.ownerLastName}"
        holder.tvStatus.text = salon.status
        holder.tvInitials.text = salon.salonName?.take(2)?.uppercase() ?: "S"
        
        holder.tvBookings.text = "${salon.bookings ?: 0} bookings"
        holder.tvServices.text = "${salon.services ?: 0} services"
        holder.tvRating.text = "${salon.rating ?: 0.0} ★"

        // Status Colors and Visibility
        when (salon.status) {
            "Active" -> {
                holder.cardStatus.setCardBackgroundColor(0xFFE7FDF0.toInt())
                holder.tvStatus.setTextColor(0xFF4CAF50.toInt())
                holder.btnSuspend.visibility = View.VISIBLE
                holder.btnAccept.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnRestore.visibility = View.GONE
            }
            "Pending" -> {
                holder.cardStatus.setCardBackgroundColor(0xFFFDF5E7.toInt())
                holder.tvStatus.setTextColor(0xFFFF9800.toInt())
                holder.btnSuspend.visibility = View.GONE
                holder.btnAccept.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
                holder.btnRestore.visibility = View.GONE
            }
            "Suspended" -> {
                holder.cardStatus.setCardBackgroundColor(0xFFFEECEB.toInt())
                holder.tvStatus.setTextColor(0xFFF44336.toInt())
                holder.btnSuspend.visibility = View.GONE
                holder.btnAccept.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnRestore.visibility = View.VISIBLE
            }
        }

        val ref = FirebaseDatabase.getInstance().getReference("Salons").child(salon.salonId!!)

        holder.btnAccept.setOnClickListener { ref.child("status").setValue("Active") }
        holder.btnSuspend.setOnClickListener { ref.child("status").setValue("Suspended") }
        holder.btnRestore.setOnClickListener { ref.child("status").setValue("Active") }
        holder.btnReject.setOnClickListener { ref.removeValue() }
    }

    override fun getItemCount() = salonList.size

    fun updateList(newList: List<SalonModel>) {
        salonList = newList
        notifyDataSetChanged()
    }
}
