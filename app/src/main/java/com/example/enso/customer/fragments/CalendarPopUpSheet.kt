package com.example.enso.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.enso.databinding.BottomSheetCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarPopUpSheet(
    private val workingHours: HashMap<String, String>,
    private val onDateSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCalendarBinding? = null
    private val binding get() = _binding!!
    private var tempSelectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("EEE", Locale.ENGLISH) // Matches "Mon", "Tue"...
        
        tempSelectedDate = sdf.format(calendar.time)

        // Set range: Today to 60 days in future
        val minDate = System.currentTimeMillis()
        val maxDate = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000)

        binding.calendarView.minDate = minDate
        binding.calendarView.maxDate = maxDate

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            
            val dayKey = dayKeyFormat.format(selectedCalendar.time)
            val hours = workingHours[dayKey] ?: "Closed"

            if (hours == "Closed") {
                Toast.makeText(requireContext(), "Salon is closed on this day", Toast.LENGTH_SHORT).show()
                binding.btnSelectDate.isEnabled = false
                binding.btnSelectDate.alpha = 0.5f
            } else {
                tempSelectedDate = sdf.format(selectedCalendar.time)
                binding.btnSelectDate.isEnabled = true
                binding.btnSelectDate.alpha = 1.0f
            }
        }

        binding.btnSelectDate.setOnClickListener {
            onDateSelected(tempSelectedDate)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CalendarPopUpSheet"
    }
}