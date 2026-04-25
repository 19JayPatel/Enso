package com.example.enso.customer.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.customer.models.BookingModel
import com.example.enso.customer.activities.MainActivity
import com.example.enso.customer.activities.ReceiptActivity
import com.example.enso.customer.adapters.CustomerBookingsAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BookingsFragment : Fragment(R.layout.fragment_bookings) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var recyclerUpcoming: RecyclerView
    private lateinit var recyclerCompleted: RecyclerView
    private lateinit var recyclerCancelled: RecyclerView

    private lateinit var adapterUpcoming: CustomerBookingsAdapter
    private lateinit var adapterCompleted: CustomerBookingsAdapter
    private lateinit var adapterCancelled: CustomerBookingsAdapter

    private val upcomingList = mutableListOf<BookingModel>()
    private val completedList = mutableListOf<BookingModel>()
    private val cancelledList = mutableListOf<BookingModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Bookings")

        setupUI(view)
        setupRecyclerViews(view)
        loadBookings()
    }

    private fun setupUI(view: View) {
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tabUpcoming = view.findViewById<LinearLayout>(R.id.tabUpcoming)
        val tabCompleted = view.findViewById<LinearLayout>(R.id.tabCompleted)
        val tabCancelled = view.findViewById<LinearLayout>(R.id.tabCancelled)

        val tvUpcoming = view.findViewById<TextView>(R.id.tvUpcoming)
        val tvCompleted = view.findViewById<TextView>(R.id.tvCompleted)
        val tvCancelled = view.findViewById<TextView>(R.id.tvCancelled)

        val indicatorUpcoming = view.findViewById<View>(R.id.indicatorUpcoming)
        val indicatorCompleted = view.findViewById<View>(R.id.indicatorCompleted)
        val indicatorCancelled = view.findViewById<View>(R.id.indicatorCancelled)

        recyclerUpcoming = view.findViewById(R.id.recyclerUpcomingBookings)
        recyclerCompleted = view.findViewById(R.id.recyclerCompletedBookings)
        recyclerCancelled = view.findViewById(R.id.recyclerCancelledBookings)

        btnBack.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.bottomNav.selectedItemId = MainActivity.NAV_HOME
            }
        }

        tabUpcoming.setOnClickListener {
            updateTabs(0, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled)
        }

        tabCompleted.setOnClickListener {
            updateTabs(1, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled)
        }

        tabCancelled.setOnClickListener {
            updateTabs(2, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled)
        }
    }

    private fun setupRecyclerViews(view: View) {
        adapterUpcoming = CustomerBookingsAdapter(upcomingList, { booking ->
            showCancelBookingDialog(booking)
        }, { booking ->
            openReceiptActivity(booking.bookingId)
        })

        adapterCompleted = CustomerBookingsAdapter(completedList, {}, { booking ->
            openReceiptActivity(booking.bookingId)
        })

        adapterCancelled = CustomerBookingsAdapter(cancelledList, {}, { booking ->
            openReceiptActivity(booking.bookingId)
        })

        recyclerUpcoming.layoutManager = LinearLayoutManager(context)
        recyclerUpcoming.adapter = adapterUpcoming

        recyclerCompleted.layoutManager = LinearLayoutManager(context)
        recyclerCompleted.adapter = adapterCompleted

        recyclerCancelled.layoutManager = LinearLayoutManager(context)
        recyclerCancelled.adapter = adapterCancelled
    }

    private fun openReceiptActivity(bookingId: String) {
        val intent = Intent(requireContext(), ReceiptActivity::class.java)
        intent.putExtra("BOOKING_ID", bookingId)
        startActivity(intent)
    }

    private fun loadBookings() {
        val currentUserId = auth.currentUser?.uid ?: return

        // FIX: Ensure correct filtering using customerId field
        val query = database.orderByChild("customerId").equalTo(currentUserId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                upcomingList.clear()
                completedList.clear()
                cancelledList.clear()

                for (data in snapshot.children) {
                    val booking = data.getValue(BookingModel::class.java)
                    booking?.let {
                        when (it.status.lowercase()) {
                            "upcoming" -> upcomingList.add(it)
                            "completed" -> completedList.add(it)
                            "cancelled" -> cancelledList.add(it)
                        }
                    }
                }

                adapterUpcoming.updateData(upcomingList)
                adapterCompleted.updateData(completedList)
                adapterCancelled.updateData(cancelledList)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateTabs(
        index: Int,
        tvUpcoming: TextView, tvCompleted: TextView, tvCancelled: TextView,
        indicatorUpcoming: View, indicatorCompleted: View, indicatorCancelled: View
    ) {
        if (!isAdded) return
        val brown = ContextCompat.getColor(requireContext(), R.color.primary_brown)
        val gray = ContextCompat.getColor(requireContext(), R.color.text_gray)

        tvUpcoming.setTextColor(if (index == 0) brown else gray)
        tvCompleted.setTextColor(if (index == 1) brown else gray)
        tvCancelled.setTextColor(if (index == 2) brown else gray)

        indicatorUpcoming.visibility = if (index == 0) View.VISIBLE else View.INVISIBLE
        indicatorCompleted.visibility = if (index == 1) View.VISIBLE else View.INVISIBLE
        indicatorCancelled.visibility = if (index == 2) View.VISIBLE else View.INVISIBLE

        recyclerUpcoming.visibility = if (index == 0) View.VISIBLE else View.GONE
        recyclerCompleted.visibility = if (index == 1) View.VISIBLE else View.GONE
        recyclerCancelled.visibility = if (index == 2) View.VISIBLE else View.GONE
    }

    private fun showCancelBookingDialog(booking: BookingModel) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_cancel_booking, null)
        dialog.setContentView(view)

        val btnConfirmCancel = view.findViewById<MaterialButton>(R.id.btnConfirmCancel)
        val btnKeepAppointment = view.findViewById<MaterialButton>(R.id.btnKeepAppointment)

        btnConfirmCancel.setOnClickListener {
            // Updated to lowercase "cancelled"
            database.child(booking.bookingId).child("status").setValue("cancelled")
                .addOnSuccessListener {
                    dialog.dismiss()
                    showSuccessDialog()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                }
        }

        btnKeepAppointment.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_booking_canceled_success, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<MaterialButton>(R.id.btnBackToBookings).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
        
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
