package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
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
    private var serviceId: String = ""
    
    // Store working hours as strings: e.g., "Mon" -> "9:00 AM – 8:00 PM"
    private var workingHoursMap = HashMap<String, String>()
    private var serviceDuration = 45 // Default 45 mins
    private var totalDuration = 0 // Combined duration of all selected services

    // Intent Data from SalonDetails
    private var salonName = ""
    private var salonAddress = ""
    private var salonRating = ""
    private var salonDistance = ""
    private var serviceName1 = ""
    private var servicePrice1 = ""
    private var serviceName2 = ""
    private var servicePrice2 = ""
    private var discount = ""
    private var totalPrice = ""
    private var serviceDurationStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // STEP 2: Receive bundle values
        salonId = intent.getStringExtra("salonId") ?: ""
        serviceId = intent.getStringExtra("serviceId") ?: ""
        
        salonName = intent.getStringExtra("salonName") ?: ""
        salonAddress = intent.getStringExtra("salonAddress") ?: ""
        salonRating = intent.getStringExtra("salonRating") ?: ""
        salonDistance = intent.getStringExtra("salonDistance") ?: ""
        
        serviceName1 = intent.getStringExtra("serviceName1") ?: ""
        servicePrice1 = intent.getStringExtra("servicePrice1") ?: ""
        
        serviceName2 = intent.getStringExtra("serviceName2") ?: ""
        servicePrice2 = intent.getStringExtra("servicePrice2") ?: ""
        
        discount = intent.getStringExtra("discount") ?: ""
        totalPrice = intent.getStringExtra("totalPrice") ?: ""
        
        serviceDurationStr = intent.getStringExtra("serviceDuration") ?: ""
        
        // Read stored value from BookingSessionManager
        totalDuration = BookingSessionManager.totalDurationMinutes
        
        BookingSessionManager.salonId = salonId
        BookingSessionManager.serviceId = serviceId
        BookingSessionManager.currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        fetchServiceDetails()
        fetchSalonDetails()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            // STEP 1: Lock the slot before moving to confirmation
            lockSlot()
        }
    }

    private fun lockSlot() {
        val currentUserId = BookingSessionManager.currentUserId
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Please login to book", Toast.LENGTH_SHORT).show()
            return
        }
        val sId = BookingSessionManager.salonId
        val date = BookingSessionManager.bookingDate
        val time = BookingSessionManager.bookingTime
        val currentTime = System.currentTimeMillis()

        val lockRef = FirebaseDatabase.getInstance().getReference("SlotLocks")
            .child(sId).child(date).child(time)

        lockRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val lockData = mutableData.value as? Map<String, Any>

                if (lockData == null) {
                    val newLock = mapOf(
                        "status" to "locked",
                        "lockedBy" to currentUserId,
                        "lockedAt" to currentTime
                    )
                    mutableData.value = newLock
                    return Transaction.success(mutableData)
                } else {
                    val lockedAt = (lockData["lockedAt"] as? Long) ?: 0L
                    val fiveMinutesInMillis = 5 * 60 * 1000

                    if (currentTime - lockedAt > fiveMinutesInMillis) {
                        val newLock = mapOf(
                            "status" to "locked",
                            "lockedBy" to currentUserId,
                            "lockedAt" to currentTime
                        )
                        mutableData.value = newLock
                        return Transaction.success(mutableData)
                    } else {
                        return Transaction.abort()
                    }
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    // STEP 3: Open BookingSummaryActivity and pass all values
                    val intent = Intent(this@DateTimeActivity, ConfirmBookingActivity::class.java)
                    
                    intent.putExtra("selectedDate", selectedBookingDate)
                    intent.putExtra("selectedTime", selectedBookingTime)
                    
                    intent.putExtra("salonName", salonName)
                    intent.putExtra("salonAddress", salonAddress)
                    intent.putExtra("salonRating", salonRating)
                    intent.putExtra("salonDistance", salonDistance)
                    
                    intent.putExtra("serviceName1", serviceName1)
                    intent.putExtra("servicePrice1", servicePrice1)
                    
                    intent.putExtra("serviceName2", serviceName2)
                    intent.putExtra("servicePrice2", servicePrice2)
                    
                    intent.putExtra("discount", discount)
                    intent.putExtra("totalPrice", totalPrice)
                    
                    intent.putExtra("serviceDuration", serviceDurationStr)


                    
                    startActivity(intent)
                } else {
                    Toast.makeText(this@DateTimeActivity, "Slot already booked by another user", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchServiceDetails() {
        if (serviceId.isEmpty()) {
            // If serviceId is empty (multiple services), use totalDuration from session manager
            if (totalDuration > 0) {
                serviceDuration = totalDuration
//                BookingSessionManager.serviceDuration = serviceDuration
            }
            return
        }
        FirebaseDatabase.getInstance().getReference("Services").child(serviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val durationStr = snapshot.child("duration").getValue(String::class.java) ?: "45"
                    // Prefer totalDuration if it was calculated from multiple services
                    if (totalDuration > 0) {
                        serviceDuration = totalDuration
                    } else {
                        serviceDuration = durationStr.toIntOrNull() ?: 45
                    }
//                    BookingSessionManager.serviceDuration = serviceDuration

                    BookingSessionManager.serviceName = snapshot.child("serviceName").getValue(String::class.java) ?: ""
                    BookingSessionManager.servicePrice = snapshot.child("price").getValue(String::class.java) ?: "0"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchSalonDetails() {
        if (salonId.isEmpty()) return

        val salonRef = FirebaseDatabase.getInstance().getReference("Salons").child(salonId)
        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                BookingSessionManager.salonName = snapshot.child("name").getValue(String::class.java) ?: ""

                val whSnapshot = snapshot.child("workingHours")
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (day in days) {
                    workingHoursMap[day] = whSnapshot.child(day).getValue(String::class.java) ?: "Closed"
                }

                generateDynamicDates()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DateTimeActivity, "Failed to load salon info", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateDynamicDates() {
        val dates = mutableListOf<DateModel>()
        val calendar = Calendar.getInstance()

        val dayKeyFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val displayDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val displayMonthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val storageFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        var foundDates = 0
        for (i in 0 until 7) {
            val dayKey = dayKeyFormat.format(calendar.time)
            val hours = workingHoursMap[dayKey] ?: "Closed"

            if (hours != "Closed") {
                dates.add(DateModel(
                    displayDayFormat.format(calendar.time).uppercase(),
                    displayMonthDayFormat.format(calendar.time),
                    "$serviceDuration mins",
                    storageDate = storageFormat.format(calendar.time)
                ))
                foundDates++
            }
            if (foundDates == 3) break
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        dates.add(DateModel("", "", "", isMoreDates = true))

        val adapter = DateAdapter(dates) { position ->
            val clickedItem = dates[position]

            if (clickedItem.isMoreDates) {
                val calendarSheet = CalendarPopUpSheet(workingHoursMap) { date: String ->
                    selectedBookingDate = date
                    BookingSessionManager.bookingDate = date
                    isDateSelected = true
                    fetchBookedSlotsAndGenerateTime()
                }
                calendarSheet.show(supportFragmentManager, CalendarPopUpSheet.TAG)
            } else {
                selectedBookingDate = clickedItem.storageDate ?: ""
                BookingSessionManager.bookingDate = selectedBookingDate
                isDateSelected = true
                fetchBookedSlotsAndGenerateTime()
            }
        }
        binding.rvDates.adapter = adapter
    }

    private fun fetchBookedSlotsAndGenerateTime() {
        if (selectedBookingDate.isEmpty()) return

        // WHY: This reads locked slots to prevent duplicate bookings
        // WHAT: Fetches data from "SlotLocks" for the specific salon and date from Firebase
        // HOW: By checking if a time exists in this path, we know it's already reserved and prevents double booking
        FirebaseDatabase.getInstance().reference
            .child("SlotLocks")
            .child(salonId)
            .child(selectedBookingDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // WHY: List used to mark booked slots inside adapter
                    // WHAT: Extracts all time keys (e.g., "9:00 AM") into a list called lockedSlotsList
                    // HOW: Converting snapshot children into a list allows the adapter to easily identify which slots to disable
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

        if (hoursRange == "Closed") {
            // WHY: Adapter needs locked slots to update UI even if empty
            // WHAT: Passes an empty list to the adapter when the salon is closed
            // HOW: Ensures the adapter is initialized correctly even without available times
            binding.rvTimeSlots.adapter = TimeSlotAdapter(emptyList(), emptyList()) {}
            return
        }

        try {
            val parts = hoursRange.split("–", "-")
            if (parts.size == 2) {
                val startTimeStr = parts[0].trim()
                val endTimeStr = parts[1].trim()

                val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                val start = timeFormat.parse(startTimeStr)
                val end = timeFormat.parse(endTimeStr)

                if (start != null && end != null) {
                    val cal = Calendar.getInstance()
                    cal.time = start

                    while (cal.time.before(end)) {
                        val slotTime = timeFormat.format(cal.time)
                        // WHY: We add all slots including locked ones so the user can see they are booked
                        // WHAT: Populates the timeSlots list with every possible appointment time for the day
                        // HOW: By including all slots, the adapter can later mark the locked ones as "Booked"
                        timeSlots.add(TimeSlotModel(slotTime))
                        cal.add(Calendar.MINUTE, serviceDuration)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // WHY: Adapter needs locked slots to update UI
        // WHAT: Pass lockedSlotsList into TimeSlotAdapter constructor along with available slots
        // HOW: This gives the adapter the data needed to lock specific slots and prevent double booking
        val adapter = TimeSlotAdapter(timeSlots, lockedSlotsList) { position ->
            selectedBookingTime = timeSlots[position].time
            BookingSessionManager.bookingTime = selectedBookingTime
            isTimeSelected = true
            updateButtonState()
        }
        binding.rvTimeSlots.adapter = adapter
        isTimeSelected = false
        updateButtonState()
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
