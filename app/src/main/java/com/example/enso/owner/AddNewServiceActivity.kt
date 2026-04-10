package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class AddNewServiceActivity : AppCompatActivity() {

    private lateinit var etDescription: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: TextView
    private lateinit var btnNext: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_service)

        // Initialize Views
        etDescription = findViewById(R.id.etDescription)
        tvCharCount = findViewById(R.id.tvCharCount)
        btnBack = findViewById(R.id.btnBack)
        btnCancel = findViewById(R.id.btnCancel)
        btnNext = findViewById(R.id.btnNext)

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
            val intent = Intent(this, AddServicePricingActivity::class.java)
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
