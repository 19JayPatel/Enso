package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class AddServiceReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_review)

        // ✅ STEP 1: RECEIVE DATA
        val serviceName = intent.getStringExtra("serviceName")
        val category = intent.getStringExtra("category")
        val description = intent.getStringExtra("description")
        val price = intent.getStringExtra("price")
        val duration = intent.getStringExtra("duration")

        // ✅ STEP 2: SHOW PREVIEW
        val tvInitials = findViewById<TextView>(R.id.tvInitials)
        val tvServiceNamePreview = findViewById<TextView>(R.id.tvServiceNamePreview)
        val tvServiceDetailsPreview = findViewById<TextView>(R.id.tvServiceDetailsPreview)
        val tvPricePreview = findViewById<TextView>(R.id.tvPricePreview)

        val tvSummaryName = findViewById<TextView>(R.id.tvSummaryName)
        val tvSummaryCategory = findViewById<TextView>(R.id.tvSummaryCategory)
        val tvSummaryDescription = findViewById<TextView>(R.id.tvSummaryDescription)
        val tvSummaryPrice = findViewById<TextView>(R.id.tvSummaryPrice)
        val tvSummaryDuration = findViewById<TextView>(R.id.tvSummaryDuration)

        // Bind Preview Card
        tvInitials.text = category?.take(1)?.uppercase() ?: "S"
        tvServiceNamePreview.text = serviceName
        tvServiceDetailsPreview.text = "$category • $duration min"
        tvPricePreview.text = price

        // Bind Summary Card
        tvSummaryName.text = serviceName
        tvSummaryCategory.text = category
        tvSummaryDescription.text = description
        tvSummaryPrice.text = "$$price.00"
        tvSummaryDuration.text = "$duration minutes"

        // Initialize Buttons
        val btnBackHeader = findViewById<ImageView>(R.id.btnBackHeader)
        val btnEdit = findViewById<TextView>(R.id.btnEdit)
        val btnPublish = findViewById<TextView>(R.id.btnPublish)

        // Header Back Button
        btnBackHeader.setOnClickListener {
            finish()
        }

        // Bottom Edit Button (Goes back to pricing step)
        btnEdit.setOnClickListener {
            finish()
        }

        // ✅ STEP 3: ON PUBLISH CLICK
        btnPublish.setOnClickListener {
            val intent = Intent(this, ServicePublishedActivity::class.java)
            intent.putExtra("serviceName", serviceName)
            intent.putExtra("category", category)
            intent.putExtra("description", description)
            intent.putExtra("price", price)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }
    }
}
