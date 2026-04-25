package com.example.enso.owner.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.owner.adapters.UpcomingAppointmentAdapter

class SalonOwnerDashboardActivity : AppCompatActivity() {

    private lateinit var rvUpcomingAppointments: RecyclerView
    private lateinit var adapter: UpcomingAppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_salon_owner_dashboard)

        // Initialize RecyclerView
        rvUpcomingAppointments = findViewById(R.id.rvDashboardAppointments)
        rvUpcomingAppointments.layoutManager = LinearLayoutManager(this)

        // Load dummy data
//        val dummyData = listOf(
//            UpcomingAppointment(
//                "Sarah Johnson",
//                "Hair Cut + Hair Wash",
//                "$45.00",
//                "9:30 AM",
//                "Confirmed",
//                "SJ"
//            ),
//            UpcomingAppointment(
//                "Mike Rodriguez",
//                "Hair Styling",
//                "$30.00",
//                "11:00 AM",
//                "Pending",
//                "MR"
//            ),
//            UpcomingAppointment(
//                "Aisha Patel",
//                "Nail Art + Manicure",
//                "$55.00",
//                "2:15 PM",
//                "Confirmed",
//                "AP"
//            )
//        )

        // Setup Adapter
//        adapter = UpcomingAppointmentAdapter(dummyData)
        rvUpcomingAppointments.adapter = adapter
    }
}