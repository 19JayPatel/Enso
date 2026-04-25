package com.example.enso.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.databinding.FragmentAdminDashboardScreenBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        updateDate()
        fetchBookingStats()
        fetchSalonsAndUsersCount()
    }

    private fun updateDate() {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate
    }

    /**
     * FIX: Fetch and count bookings.
     * Note: Since the XML doesn't have specific IDs for stats yet,
     * we will find them by their structure in the GridLayout.
     */
    private fun fetchBookingStats() {
        val database = FirebaseDatabase.getInstance().getReference("Bookings")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                for (data in snapshot.children) {
                    total++
                }

                if (_binding != null) {
                    // Total Bookings is the first card in GridLayout (index 0)
                    val card = binding.statsGrid.getChildAt(0) as? androidx.cardview.widget.CardView
                    val layout = card?.getChildAt(0) as? android.widget.LinearLayout
                    val tvCount = layout?.getChildAt(1) as? TextView
                    tvCount?.text = total.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchSalonsAndUsersCount() {
        // Fetch Total Salons (index 1 in GridLayout)
        FirebaseDatabase.getInstance().getReference("Salons").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null) {
                    val card = binding.statsGrid.getChildAt(1) as? androidx.cardview.widget.CardView
                    val layout = card?.getChildAt(0) as? android.widget.LinearLayout
                    val tvCount = layout?.getChildAt(1) as? TextView
                    tvCount?.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Total Users (index 2 in GridLayout)
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null) {
                    val card = binding.statsGrid.getChildAt(2) as? androidx.cardview.widget.CardView
                    val layout = card?.getChildAt(0) as? android.widget.LinearLayout
                    val tvCount = layout?.getChildAt(1) as? TextView
                    tvCount?.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
