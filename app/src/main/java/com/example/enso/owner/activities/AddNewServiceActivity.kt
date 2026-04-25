package com.example.enso.owner.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R
import com.example.enso.owner.activities.AddServicePricingActivity

class AddNewServiceActivity : AppCompatActivity() {

    private lateinit var etServiceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: TextView
    private lateinit var btnNext: TextView
    private lateinit var spinnerCategory: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_service)

        // Initialize Views
        etServiceName = findViewById(R.id.etServiceName)
        etDescription = findViewById(R.id.etDescription)
        tvCharCount = findViewById(R.id.tvCharCount)
        btnBack = findViewById(R.id.btnBack)
        btnCancel = findViewById(R.id.btnCancel)
        btnNext = findViewById(R.id.btnNext)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        // ✅ STEP 1: CREATE CATEGORY LIST
        val categoryList = listOf(
            "Hair",
            "Nails",
            "Skin Care",
            "Spa & Wellness",
            "Makeup"
        )

        // ✅ STEP 2: CREATE ADAPTER
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // ✅ STEP 3: SET ADAPTER TO SPINNER
        spinnerCategory.adapter = adapter

        // Back Button functionality
        btnBack.setOnClickListener {
            finish()
        }

        // Cancel Button functionality
        btnCancel.setOnClickListener {
            finish()
        }

        // Next Button functionality - Navigate to Step 2 (Pricing)
        btnNext.setOnClickListener {
            // ✅ STEP 1: GET INPUTS
            val serviceName = etServiceName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val description = etDescription.text.toString().trim()

            // ✅ STEP 2: VALIDATION
            if (serviceName.isEmpty()) {
                etServiceName.error = "Service name is required"
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ STEP 3: SEND DATA
            val intent = Intent(this, AddServicePricingActivity::class.java)
            intent.putExtra("serviceName", serviceName)
            intent.putExtra("category", category)
            intent.putExtra("description", description)
            startActivity(intent)
        }

        // Live Character Counter for Description
        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                tvCharCount.text = "$currentLength / 200"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}