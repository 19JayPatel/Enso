package com.example.enso.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.enso.R
import com.example.enso.customer.adapters.Stylist
import com.example.enso.customer.adapters.StylistAdapter
import com.example.enso.databinding.ActivityChooseStylistBinding

class ChooseStylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseStylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseStylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, DateTimeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val stylists = listOf(
            Stylist(1, "Any Stylist", "Next available stylist", R.drawable.ic_group_outline, true),
            Stylist(2, "John Doe", "Hair Specialist", R.drawable.johndoe),
            Stylist(3, "Anna Lee", "Hair Dresser", R.drawable.annalee),
            Stylist(4, "Ella Ford", "Hair Specialist", R.drawable.ellaford),
            Stylist(5, "Marsh Donnell", "Hair Specialist", R.drawable.marshdonnell)
        )

        val adapter = StylistAdapter(stylists) { isEnabled ->
            binding.btnContinue.isEnabled = isEnabled
            if (isEnabled) {
                binding.btnContinue.setAlpha(1.0f)
            } else {
                binding.btnContinue.setAlpha(0.5f)
            }
        }

        binding.rvStylists.layoutManager = LinearLayoutManager(this)
        binding.rvStylists.adapter = adapter
    }
}