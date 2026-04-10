package com.example.enso.owner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R

class ServicePublishedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_published)

        // Initialize Views
        val btnAddAnother = findViewById<TextView>(R.id.btnAddAnother)
        val btnBackToServices = findViewById<TextView>(R.id.btnBackToServices)

        // Add Another Service: Navigate back to the start of the flow
        btnAddAnother.setOnClickListener {
            val intent = Intent(this, AddNewServiceActivity::class.java)
            // Clear the task stack so they start fresh
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Back to Services: Close this activity and go back to the list
        btnBackToServices.setOnClickListener {
            finish()
        }
    }
}
