package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class AddServiceReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_review)

        // Initialize Views
        val btnBackHeader = findViewById<android.widget.ImageView>(R.id.btnBackHeader)
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

        // Publish Button - Navigate to Success Screen
        btnPublish.setOnClickListener {
            val intent = Intent(this, ServicePublishedActivity::class.java)
            startActivity(intent)
        }
    }
}
