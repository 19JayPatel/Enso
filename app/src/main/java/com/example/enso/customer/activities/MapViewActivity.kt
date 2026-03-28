package com.example.enso.customer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.databinding.ActivityMapViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MapViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Initialize BottomSheet
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.filterBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true

        binding.btnFilter.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        binding.btnApply.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}