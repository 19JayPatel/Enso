package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.enso.R

class AddServicePricingActivity : AppCompatActivity() {

    private lateinit var etPrice: EditText
    private lateinit var etCustomDuration: EditText
    private var selectedDuration: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_pricing)

        // ✅ STEP 1: RECEIVE PREVIOUS DATA
        val serviceName = intent.getStringExtra("serviceName")
        val category = intent.getStringExtra("category")
        val description = intent.getStringExtra("description")

        // Initialize Views
        etPrice = findViewById(R.id.etPrice)
        etCustomDuration = findViewById(R.id.etCustomDuration)
        val btnBackHeader = findViewById<android.widget.ImageView>(R.id.btnBackHeader)
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnNext = findViewById<TextView>(R.id.btnNext)

        setupDurationChips()

        // Header Back Button
        btnBackHeader.setOnClickListener {
            finish()
        }

        // Bottom Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Next Button - Navigate to Step 3 (Review)
        btnNext.setOnClickListener {
            // ✅ STEP 2: GET INPUT
            val price = etPrice.text.toString().trim()
            val customDur = etCustomDuration.text.toString().trim()

            val finalDuration = if (customDur.isNotEmpty()) customDur else selectedDuration

            // ✅ STEP 3: VALIDATION
            if (price.isEmpty() || finalDuration.isEmpty()) {
                Toast.makeText(this, "Enter price & duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ STEP 4: SEND TO NEXT
            val intent = Intent(this, AddServiceReviewActivity::class.java)
            intent.putExtra("serviceName", serviceName)
            intent.putExtra("category", category)
            intent.putExtra("description", description)
            intent.putExtra("price", price)
            intent.putExtra("duration", finalDuration)
            startActivity(intent)
        }
    }

    private fun setupDurationChips() {
        val chips = listOf<TextView>(
            findViewById(R.id.tv15min),
            findViewById(R.id.tv30min),
            findViewById(R.id.tv45min),
            findViewById(R.id.tv60min),
            findViewById(R.id.tv90min)
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                // Reset all
                chips.forEach {
                    it.setBackgroundResource(R.drawable.bg_chip_unselected)
                    it.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
                }
                // Select clicked
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))

                selectedDuration = chip.text.toString().replace(" min", "")
                etCustomDuration.setText("") // Clear custom if chip selected
            }
        }
    }
}
