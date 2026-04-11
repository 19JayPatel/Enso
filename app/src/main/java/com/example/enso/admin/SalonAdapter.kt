package com.example.enso.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

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
        
        if (salon.ownerName.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(salon.ownerId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        holder.tvOwnerName.text = "Owner: $name"
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            holder.tvOwnerName.text = "Owner: " + salon.ownerName
        }
        
        holder.tvStatus.text = salon.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        holder.tvInitials.text = if (salon.salonName.isNotEmpty()) {
            val words = salon.salonName.trim().split(" ")
            if (words.size >= 2) {
                (words[0].take(1) + words[1].take(1)).uppercase()
            } else {
                salon.salonName.take(2).uppercase()
            }
        } else {
            "S"
        }

        // Visibility Logic
        if (salon.status == "pending") {
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnReject.visibility = View.VISIBLE
            holder.btnSuspend.visibility = View.GONE
            holder.btnRestore.visibility = View.GONE
            holder.cardStatus.setCardBackgroundColor(0xFFFDF5E7.toInt())
            holder.tvStatus.setTextColor(0xFFFF9800.toInt())
        } else if (salon.status == "active") {
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
            holder.btnSuspend.visibility = View.VISIBLE
            holder.btnRestore.visibility = View.GONE
            holder.cardStatus.setCardBackgroundColor(0xFFE7FDF0.toInt())
            holder.tvStatus.setTextColor(0xFF4CAF50.toInt())
        } else if (salon.status == "suspended") {
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
            holder.btnSuspend.visibility = View.GONE
            holder.btnRestore.visibility = View.VISIBLE
            holder.cardStatus.setCardBackgroundColor(0xFFFEECEB.toInt())
            holder.tvStatus.setTextColor(0xFFF44336.toInt())
        }

        // Click Listeners
        holder.btnAccept.setOnClickListener {
            val db = FirebaseDatabase.getInstance()
            db.getReference("Salons").child(salon.salonId).child("status").setValue("active")
            db.getReference("Users").child(salon.ownerId).child("status").setValue("active")
        }

        holder.btnReject.setOnClickListener {
            val db = FirebaseDatabase.getInstance()
            db.getReference("Salons").child(salon.salonId).removeValue()
            db.getReference("Users").child(salon.ownerId).removeValue()
        }

        holder.btnSuspend.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Salons")
                .child(salon.salonId).child("status").setValue("suspended")
        }

        holder.btnRestore.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Salons")
                .child(salon.salonId).child("status").setValue("active")
        }
    }

    override fun getItemCount() = salonList.size
}