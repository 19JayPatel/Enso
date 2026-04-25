package com.example.enso.owner.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enso.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ServicePublishedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_published)

        // ✅ STEP 1: GET DATA
        val serviceName = intent.getStringExtra("serviceName")
        val category = intent.getStringExtra("category")
        val description = intent.getStringExtra("description")
        val price = intent.getStringExtra("price")
        val duration = intent.getStringExtra("duration")

        // ✅ STEP 2: GET USER
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // ✅ STEP 3, 4, 5: SAVE TO FIREBASE
        if (userId != null) {
            saveServiceToFirebase(userId, serviceName, category, description, price, duration)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Initialize Views
        val btnAddAnother = findViewById<TextView>(R.id.btnAddAnother)
        val btnBackToServices = findViewById<TextView>(R.id.btnBackToServices)

        // Add Another Service: Navigate back to the start of the flow
        btnAddAnother.setOnClickListener {
            val intent = Intent(this, AddNewServiceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Back to Services: Close this activity and go back to the list
        btnBackToServices.setOnClickListener {
            finish()
        }
    }

    /**
     * BUG 2 FIX: Save service under owner ID as requested.
     * Path: Services / ownerId / serviceId
     */
    private fun saveServiceToFirebase(
        userId: String,
        serviceName: String?,
        category: String?,
        description: String?,
        price: String?,
        duration: String?
    ) {
        // ✅ STEP 3: FIREBASE REF (Save under ownerId node)
        val db = FirebaseDatabase.getInstance().getReference("Services").child(userId)
        val serviceId = db.push().key ?: return

        // ✅ STEP 4: CREATE DATA MAP
        val serviceMap = HashMap<String, Any>()
        serviceMap["serviceId"] = serviceId
        serviceMap["ownerId"] = userId
        serviceMap["serviceName"] = serviceName ?: ""
        serviceMap["category"] = category ?: ""
        serviceMap["description"] = description ?: ""
        serviceMap["price"] = price ?: ""
        serviceMap["duration"] = duration ?: ""
        serviceMap["status"] = "active"
        serviceMap["createdAt"] = System.currentTimeMillis()

        // ✅ STEP 5: SAVE
        db.child(serviceId).setValue(serviceMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Service Added Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add service: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}