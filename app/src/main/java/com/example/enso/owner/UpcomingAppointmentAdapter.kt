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
// WHAT: Change adapter dataset to MutableList<BookingModel> and add listener
// WHY: Realtime Firebase updates require mutable dataset. Listener needed for navigation.
// HOW: Replace List with MutableList in constructor and add OnViewClickListener parameter.
class UpcomingAppointmentAdapter(
    private val bookingList: MutableList<BookingModel>,
    private val listener: OnViewClickListener
) : RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder>() {

    // STEP 2:
    // WHAT: Create interface inside adapter
    // WHY: Fragment must receive bookingId when user taps View button.
    // HOW: Define OnViewClickListener with onViewClicked method.
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

    /**
     * WHAT: Function to update the data list and refresh the UI.
     * WHY: When users switch tabs or data changes in Firebase, we need to show the new list.
     */
    fun updateList(newList: List<BookingModel>) {

        // WHAT: Clear previous adapter dataset
        // WHY: Prevent duplicate RecyclerView entries
        // HOW: Reset adapter list before inserting Firebase snapshot data

        bookingList.clear()

        // WHAT: Insert new filtered bookings
        // WHY: Replace adapter dataset with latest Firebase data
        // HOW: Add all new bookings into adapter list

        bookingList.addAll(newList)

        // WHAT: Notify RecyclerView dataset changed
        // WHY: Refresh dashboard UI immediately
        // HOW: Trigger ViewHolder rebinding cycle

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = bookingList[position]

        // STEP 1, 2, 3, 4, 5:
        // WHAT: Fetch customer name using appointment.userId
        // WHY: Replace unreadable Firebase UID with human-friendly display name
        // HOW: Read Users/{userId}/name once during ViewHolder binding using addListenerForSingleValueEvent.
        holder.tvName.text = "Loading..." // Temporary placeholder
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(appointment.userId)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java) ?: "Unknown Customer"
                    holder.tvName.text = name
                    
                    // WHAT: Set initials based on the fetched name
                    // WHY: Visual consistency with other parts of the app
                    // HOW: Extract first letter of the name
                    holder.tvInitials.text = if (name != "Unknown Customer") {
                        name.take(1).uppercase()
                    } else {
                        "U"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.tvName.text = "Unknown Customer"
                    holder.tvInitials.text = "U"
                }
            })
        
        // WHAT: Mapping Firebase data fields to the UI components
        holder.tvService.text = appointment.services
        holder.tvPrice.text = "₹${appointment.grandTotal ?: 0}"
        holder.tvTime.text = appointment.bookingTime
        holder.tvStatus.text = appointment.status.replaceFirstChar { it.uppercase() }

        // WHAT: Changing status colors dynamically based on the booking status
        if (appointment.status.equals("confirmed", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Green for confirmed
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#E7FDF0"))
        } else if (appointment.status.equals("pending", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00")) // Orange for pending
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FDF5E7"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#8E8E8E")) // Gray for others
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
        }
        
        // WHAT: Assigning background colors based on position for a better visual variety
        val bgColors = listOf("#F4E8E1", "#E7F3FD", "#E7FDF0")
        val textColors = listOf("#A46C54", "#1976D2", "#2E7D32")
        val colorIndex = position % bgColors.size
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[colorIndex]))
        holder.tvInitials.setTextColor(Color.parseColor(textColors[colorIndex]))

        // STEP 1:
        // WHAT: Add click listener to View button.
        // WHY: Needed to identify which booking must be highlighted later.
        // HOW: Pass bookingId through callback interface onViewClicked.
        holder.btnView.setOnClickListener {
            // WHAT: Capture selected bookingId
            // WHY: Needed to identify which booking must be highlighted later
            // HOW: Pass bookingId through callback interface
            listener.onViewClicked(appointment.bookingId)
        }
    }

    override fun getItemCount() = bookingList.size
}
