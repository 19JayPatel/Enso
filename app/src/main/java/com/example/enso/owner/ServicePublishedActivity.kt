package com.example.enso.owner

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
     * private fun saveServiceToFirebase(
        userId: String,
        serviceName: String?,
        category: String?,
        description: String?,
        price: String?,
        duration: String?
    ) {
        // ✅ STEP 3: FIREBASE REF
        val db = FirebaseDatabase.getInstance().getReference("Services")
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
    */

    // WHY:
// Services must belong to a salon, not directly to the owner.
// Admin analytics, booking flow, and service filtering depend on salonId.
// Using ownerId breaks multi-salon support for a single owner.

// WHAT:
// This function fetches salonId using the logged-in ownerId,
// then stores the service mapped correctly under salonId.

// HOW:
// 1. Read current FirebaseAuth UID
// 2. Query Salons node where ownerId == UID
// 3. Extract salonId
// 4. Save service with salonId instead of ownerId

    private fun saveServiceToFirebase(
        userId: String,
        serviceName: String?,
        category: String?,
        description: String?,
        price: String?,
        duration: String?
    ) {

        val salonsRef = FirebaseDatabase.getInstance().getReference("Salons")

        salonsRef.orderByChild("ownerId")
            .equalTo(userId)
            .limitToFirst(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {

                    Toast.makeText(
                        this,
                        "Salon not found for this owner",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                val salonId = snapshot.children.first().key ?: return@addOnSuccessListener


                // STEP 2: SAVE SERVICE USING salonId

                val servicesRef =
                    FirebaseDatabase.getInstance().getReference("Services")

                val serviceId =
                    servicesRef.push().key ?: return@addOnSuccessListener


                val serviceMap = HashMap<String, Any>()

                serviceMap["serviceId"] = serviceId
                serviceMap["salonId"] = salonId   // ✅ CORRECT RELATIONSHIP
                serviceMap["serviceName"] = serviceName ?: ""
                serviceMap["category"] = category ?: ""
                serviceMap["description"] = description ?: ""
                serviceMap["price"] = price ?: ""
                serviceMap["duration"] = duration ?: ""
                serviceMap["status"] = "active"
                serviceMap["createdAt"] = System.currentTimeMillis()


                servicesRef.child(serviceId)
                    .setValue(serviceMap)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Service Added Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Failed to add service: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }
    }
}
