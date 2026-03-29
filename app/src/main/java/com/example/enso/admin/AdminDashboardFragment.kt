package com.example.enso.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.enso.databinding.FragmentAdminDashboardScreenBinding
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
    }

    private fun updateDate() {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}