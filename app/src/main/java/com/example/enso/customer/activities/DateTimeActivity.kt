package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.customer.BookingSessionManager
import com.example.enso.customer.adapters.DateAdapter
import com.example.enso.customer.adapters.DateModel
import com.example.enso.customer.adapters.TimeSlotAdapter
import com.example.enso.customer.adapters.TimeSlotModel
import com.example.enso.customer.fragments.CalendarPopUpSheet
import com.example.enso.databinding.ActivityDateTimeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class DateTimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDateTimeBinding
    private var isDateSelected = false
    private var isTimeSelected = false
    private var selectedBookingDate: String = ""
    private var selectedBookingTime: String = ""
    private var salonId: String = ""
    private var ownerId: String = ""
    
    // Store working hours: e.g., "Mon" -> "09:00 AM - 08:00 PM"
    private var workingHoursMap = HashMap<String, String>()
    private var serviceDuration = 45 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive data
        salonId = intent.getStringExtra("salonId") ?: BookingSessionManager.salonId
        ownerId = intent.getStringExtra("ownerId") ?: BookingSessionManager.ownerId
        
        BookingSessionManager.salonId = salonId
        BookingSessionManager.ownerId = ownerId
        BookingSessionManager.currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        fetchServiceDetails()
        fetchSalonDetails()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            navigateToConfirmation()
        }
    }

    private fun navigateToConfirmation() {
        val intent = Intent(this, ConfirmBookingActivity::class.java)
        // Ensure data is in session manager
        BookingSessionManager.bookingDate = selectedBookingDate
        BookingSessionManager.bookingTime = selectedBookingTime
        startActivity(intent)
    }

    private fun fetchServiceDetails() {
        // Use duration from session manager if available
        if (BookingSessionManager.totalDurationMinutes > 0) {
            serviceDuration = BookingSessionManager.totalDurationMinutes
        }
    }

    private fun fetchSalonDetails() {
        if (salonId.isEmpty()) return

        val salonRef = FirebaseDatabase.getInstance().getReference("Salons").child(salonId)
        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Structure expected: Salons -> salonId -> workingHours -> Mon -> "09:00 AM - 08:00 PM"
                val whSnapshot = snapshot.child("workingHours")
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (day in days) {
                    val hours = whSnapshot.child(day).getValue(String::class.java) ?: "Closed"
                    workingHoursMap[day] = hours
                }
                generateDynamicDates()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun generateDynamicDates() {
        val dates = mutableListOf<DateModel>()
        val calendar = Calendar.getInstance()

        val dayKeyFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val displayDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val displayMonthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val storageFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Show next 7 days in the horizontal list
        for (i in 0 until 7) {
            val dayKey = dayKeyFormat.format(calendar.time)
            val hours = workingHoursMap[dayKey] ?: "Closed"

            dates.add(DateModel(
                displayDayFormat.format(calendar.time).uppercase(),
                displayMonthDayFormat.format(calendar.time),
                if (hours == "Closed") "Closed" else "$serviceDuration mins",
                storageDate = storageFormat.format(calendar.time),
                isMoreDates = false
            ))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Add "More" option for Calendar PopUp
        dates.add(DateModel("", "", "", isMoreDates = true))

        val adapter = DateAdapter(dates) { position ->
            val clickedItem = dates[position]

            if (clickedItem.isMoreDates) {
                val calendarSheet = CalendarPopUpSheet(workingHoursMap) { date: String ->
                    handleDateSelection(date)
                }
                calendarSheet.show(supportFragmentManager, CalendarPopUpSheet.TAG)
            } else {
                handleDateSelection(clickedItem.storageDate ?: "")
            }
        }
        binding.rvDates.adapter = adapter
    }

    private fun handleDateSelection(date: String) {
        selectedBookingDate = date
        BookingSessionManager.bookingDate = date
        
        // Check if salon is closed on selected date
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val selectedDateObj = try { sdfDate.parse(date) } catch (e: Exception) { null } ?: return
        val dayKey = dayKeyFormat.format(selectedDateObj)
        val hours = workingHoursMap[dayKey] ?: "Closed"

        if (hours == "Closed") {
            Toast.makeText(this, "Salon is closed on this day", Toast.LENGTH_SHORT).show()
            isDateSelected = false
            binding.rvTimeSlots.visibility = View.GONE
        } else {
            isDateSelected = true
            binding.rvTimeSlots.visibility = View.VISIBLE
            fetchBookedSlotsAndGenerateTime()
        }
        updateButtonState()
    }

    private fun fetchBookedSlotsAndGenerateTime() {
        val lockRef = FirebaseDatabase.getInstance().getReference("SlotLocks")
            .child(salonId).child(selectedBookingDate)

        lockRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lockedSlotsList = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { lockedSlotsList.add(it) }
                }
                generateTimeSlots(lockedSlotsList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun generateTimeSlots(lockedSlotsList: List<String>) {
        val timeSlots = mutableListOf<TimeSlotModel>()

        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val selectedDateObj = try { sdfDate.parse(selectedBookingDate) } catch (e: Exception) { null } ?: return
        val dayKey = dayKeyFormat.format(selectedDateObj)
        val hoursRange = workingHoursMap[dayKey] ?: "Closed"

        if (hoursRange != "Closed") {
            try {
                val parts = hoursRange.split("–", "-")
                if (parts.size == 2) {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                    val start = timeFormat.parse(parts[0].trim())
                    val end = timeFormat.parse(parts[1].trim())

                    if (start != null && end != null) {
                        val cal = Calendar.getInstance()
                        cal.time = start

                        while (cal.time.before(end)) {
                            val slotTime = timeFormat.format(cal.time)
                            timeSlots.add(TimeSlotModel(slotTime))
                            cal.add(Calendar.MINUTE, serviceDuration)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        val adapter = TimeSlotAdapter(timeSlots, lockedSlotsList) { position ->
            selectedBookingTime = timeSlots[position].time
            BookingSessionManager.bookingTime = selectedBookingTime
            isTimeSelected = true
            updateButtonState()
        }
        binding.rvTimeSlots.adapter = adapter
    }

    private fun updateButtonState() {
        if (isDateSelected && isTimeSelected) {
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.alpha = 1.0f
        } else {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.alpha = 0.5f
        }
    }
}
