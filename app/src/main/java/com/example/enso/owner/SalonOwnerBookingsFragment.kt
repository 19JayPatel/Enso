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
 * Salon Owner Bookings Management Fragment.
 * Features: Tabs for Upcoming, Completed, Cancelled bookings.
 * Functionality: Mark as Complete, Cancel booking, Grouping by Date.
 */
class SalonOwnerBookingsFragment : Fragment() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var adapter: BookingsAdapter
    private var allBookings = mutableListOf<Booking>()
    
    // UI Elements for Tabs
    private lateinit var tvUpcoming: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvCancelled: TextView
    private lateinit var lineUpcoming: View
    private lateinit var lineCompleted: View
    private lateinit var lineCancelled: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_bookings, container, false)

        // Initialize UI Elements
        rvBookings = view.findViewById(R.id.rvBookings)
        tvUpcoming = view.findViewById(R.id.tvUpcoming)
        tvCompleted = view.findViewById(R.id.tvCompleted)
        tvCancelled = view.findViewById(R.id.tvCancelled)
        lineUpcoming = view.findViewById(R.id.lineUpcoming)
        lineCompleted = view.findViewById(R.id.lineCompleted)
        lineCancelled = view.findViewById(R.id.lineCancelled)

        // Load Dummy Data
        loadDummyData()

        // Setup RecyclerView
        rvBookings.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookingsAdapter(allBookings, 
            onComplete = { booking -> updateBookingStatus(booking, "Completed") },
            onCancel = { booking -> updateBookingStatus(booking, "Cancelled") }
        )
        rvBookings.adapter = adapter

        // Setup Tab Clicks
        view.findViewById<View>(R.id.tabUpcoming).setOnClickListener { selectTab("Upcoming") }
        view.findViewById<View>(R.id.tabCompleted).setOnClickListener { selectTab("Completed") }
        view.findViewById<View>(R.id.tabCancelled).setOnClickListener { selectTab("Cancelled") }

        // Initial Filter
        filterBookings("Upcoming")

        return view
    }

    private fun loadDummyData() {
        allBookings = mutableListOf(
            Booking("Sarah Johnson", "Hair Cut + Hair Wash", "Emma K.", "9:30 AM", "$45.00", "Confirmed", "SJ", "TODAY • SEP 10, 2024"),
            Booking("Mike Rodriguez", "Hair Styling", "James T.", "11:00 AM", "$30.00", "Pending", "MR", "TODAY • SEP 10, 2024"),
            Booking("Aisha Patel", "Nail Art + Manicure", "Priya S.", "2:15 PM", "$55.00", "Confirmed", "AP", "TOMORROW • SEP 11, 2024"),
            Booking("Lena Martinez", "Facial Treatment", "Zara A.", "4:00 PM", "$65.00", "Confirmed", "LM", "TOMORROW • SEP 11, 2024")
        )
    }

    private fun selectTab(tab: String) {
        // Reset Styles
        val gray = Color.parseColor("#8E8E8E")
        val brown = Color.parseColor("#A37551")

        tvUpcoming.setTextColor(gray)
        tvCompleted.setTextColor(gray)
        tvCancelled.setTextColor(gray)
        tvUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvCompleted.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvCancelled.setTypeface(null, android.graphics.Typeface.NORMAL)
        lineUpcoming.setBackgroundColor(Color.TRANSPARENT)
        lineCompleted.setBackgroundColor(Color.TRANSPARENT)
        lineCancelled.setBackgroundColor(Color.TRANSPARENT)

        // Set Active Style
        when (tab) {
            "Upcoming" -> {
                tvUpcoming.setTextColor(brown)
                tvUpcoming.setTypeface(null, android.graphics.Typeface.BOLD)
                lineUpcoming.setBackgroundColor(brown)
            }
            "Completed" -> {
                tvCompleted.setTextColor(brown)
                tvCompleted.setTypeface(null, android.graphics.Typeface.BOLD)
                lineCompleted.setBackgroundColor(brown)
            }
            "Cancelled" -> {
                tvCancelled.setTextColor(brown)
                tvCancelled.setTypeface(null, android.graphics.Typeface.BOLD)
                lineCancelled.setBackgroundColor(brown)
            }
        }

        filterBookings(tab)
    }

    private fun filterBookings(tab: String) {
        val filteredList = when (tab) {
            "Upcoming" -> allBookings.filter { it.status == "Confirmed" || it.status == "Pending" }
            "Completed" -> allBookings.filter { it.status == "Completed" }
            "Cancelled" -> allBookings.filter { it.status == "Cancelled" }
            else -> allBookings
        }
        adapter.updateList(filteredList)
    }

    private fun updateBookingStatus(booking: Booking, newStatus: String) {
        val index = allBookings.indexOf(booking)
        if (index != -1) {
            allBookings[index].status = newStatus
            // Determine currently active tab and refresh
            val activeTab = if (tvUpcoming.currentTextColor == Color.parseColor("#A37551")) "Upcoming"
            else if (tvCompleted.currentTextColor == Color.parseColor("#A37551")) "Completed"
            else "Cancelled"
            
            filterBookings(activeTab)
        }
    }
}

/**
 * Data Model for Booking
 */
data class Booking(
    val name: String,
    val service: String,
    val stylist: String,
    val time: String,
    val price: String,
    var status: String,
    val initials: String,
    val dateGroup: String
)

/**
 * RecyclerView Adapter for Bookings
 */
class BookingsAdapter(
    private var bookings: List<Booking>,
    private val onComplete: (Booking) -> Unit,
    private val onCancel: (Booking) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateHeader: TextView = view.findViewById(R.id.tvDateHeader)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvStylistName: TextView = view.findViewById(R.id.tvStylistName)
        val tvBookingTime: TextView = view.findViewById(R.id.tvBookingTime)
        val tvBookingPrice: TextView = view.findViewById(R.id.tvBookingPrice)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val btnMarkComplete: TextView = view.findViewById(R.id.btnMarkComplete)
        val btnCancel: TextView = view.findViewById(R.id.btnCancel)
        val llActions: View = view.findViewById(R.id.llActions)
        val dividerAction: View = view.findViewById(R.id.dividerAction)
        val cvInitials: CardView = view.findViewById(R.id.cvInitials)
    }

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_owner, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Date Header logic
        if (position == 0 || bookings[position - 1].dateGroup != booking.dateGroup) {
            holder.tvDateHeader.visibility = View.VISIBLE
            holder.tvDateHeader.text = booking.dateGroup
        } else {
            holder.tvDateHeader.visibility = View.GONE
        }

        holder.tvInitials.text = booking.initials
        holder.tvCustomerName.text = booking.name
        holder.tvServiceName.text = booking.service
        holder.tvStylistName.text = "Stylist: ${booking.stylist}"
        holder.tvBookingTime.text = booking.time
        holder.tvBookingPrice.text = booking.price
        holder.tvStatusBadge.text = booking.status

        // Status Badge Style Fix
        when (booking.status) {
            "Confirmed" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed)
            }
            "Pending" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF6C00"))
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            }
            "Completed" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#8E8E8E"))
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
            }
            "Cancelled" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#D32F2F"))
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed) // Reusing light gray bg for cancelled status text
            }
        }

        // Action Buttons Visibility
        if (booking.status == "Confirmed" || booking.status == "Pending") {
            holder.llActions.visibility = View.VISIBLE
            holder.dividerAction.visibility = View.VISIBLE
        } else {
            holder.llActions.visibility = View.GONE
            holder.dividerAction.visibility = View.GONE
        }

        holder.btnMarkComplete.setOnClickListener { onComplete(booking) }
        holder.btnCancel.setOnClickListener { onCancel(booking) }

        // Avatar variety
        val bgColors = listOf("#FDEEE7", "#E7F3FD", "#E7FDF0", "#FDF5E7")
        holder.cvInitials.setCardBackgroundColor(Color.parseColor(bgColors[position % bgColors.size]))
    }

    override fun getItemCount() = bookings.size
}
