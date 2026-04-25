package com.example.enso.customer.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val llUpcomingSection = view.findViewById<LinearLayout>(R.id.llUpcomingSection)
        val llCompletedSection = view.findViewById<LinearLayout>(R.id.llCompletedSection)
        val llCancelledSection = view.findViewById<LinearLayout>(R.id.llCancelledSection)

        val btnCancelBooking1 = view.findViewById<MaterialButton>(R.id.btnCancelBooking1)
        val btnCancelBooking2 = view.findViewById<MaterialButton>(R.id.btnCancelBooking2)

        btnBack.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.bottomNav.selectedItemId = MainActivity.NAV_HOME
            }
        }

        tabUpcoming.setOnClickListener {
            updateTabs(0, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled, llUpcomingSection, llCompletedSection, llCancelledSection)
        }

        tabCompleted.setOnClickListener {
            updateTabs(1, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled, llUpcomingSection, llCompletedSection, llCancelledSection)
        }

        tabCancelled.setOnClickListener {
            updateTabs(2, tvUpcoming, tvCompleted, tvCancelled, indicatorUpcoming, indicatorCompleted, indicatorCancelled, llUpcomingSection, llCompletedSection, llCancelledSection)
        }

        btnCancelBooking1.setOnClickListener {
            showCancelBookingDialog()
        }

        btnCancelBooking2.setOnClickListener {
            showCancelBookingDialog()
        }
    }

    private fun updateTabs(
        index: Int,
        tvUpcoming: TextView, tvCompleted: TextView, tvCancelled: TextView,
        indicatorUpcoming: View, indicatorCompleted: View, indicatorCancelled: View,
        llUpcomingSection: LinearLayout, llCompletedSection: LinearLayout, llCancelledSection: LinearLayout
    ) {
        val brown = ContextCompat.getColor(requireContext(), R.color.primary_brown)
        val gray = ContextCompat.getColor(requireContext(), R.color.text_gray)

        tvUpcoming.setTextColor(if (index == 0) brown else gray)
        tvCompleted.setTextColor(if (index == 1) brown else gray)
        tvCancelled.setTextColor(if (index == 2) brown else gray)

        indicatorUpcoming.visibility = if (index == 0) View.VISIBLE else View.INVISIBLE
        indicatorCompleted.visibility = if (index == 1) View.VISIBLE else View.INVISIBLE
        indicatorCancelled.visibility = if (index == 2) View.VISIBLE else View.INVISIBLE

        llUpcomingSection.visibility = if (index == 0) View.VISIBLE else View.GONE
        llCompletedSection.visibility = if (index == 1) View.VISIBLE else View.GONE
        llCancelledSection.visibility = if (index == 2) View.VISIBLE else View.GONE
    }

    private fun showCancelBookingDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_cancel_booking, null)
        dialog.setContentView(view)

        val btnConfirmCancel = view.findViewById<MaterialButton>(R.id.btnConfirmCancel)
        val btnKeepAppointment = view.findViewById<MaterialButton>(R.id.btnKeepAppointment)

        btnConfirmCancel.setOnClickListener {
            dialog.dismiss()
            showSuccessDialog()
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
        
        // Adjust width to match design (adding some margin)
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}