package com.example.enso.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.enso.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddSalonActivity : AppCompatActivity() {

    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var salonRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    // Salon Details
    private lateinit var etSalonName: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner

    // Owner Information
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText

    // Address
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etState: EditText
    private lateinit var etZip: EditText
    private lateinit var etCountry: EditText

    // Buttons
    private lateinit var btnBack: CardView
    private lateinit var btnRegisterSalon: Button

    private var userId: String? = null
    private var ownerName: String = ""
    
    // WHY: edit mode detection is required to check if we should load existing data or start fresh.
    // WHAT: This ID is received from the intent extra "salonId".
    // HOW: If not null, the screen title changes and existing data is fetched from Firebase.
    private var existingSalonId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_salon)

        // 1. 📌 INITIALIZE FIREBASE
        database = FirebaseDatabase.getInstance()
        salonRef = database.getReference("Salons")
        userRef = database.getReference("Users")

        // Get IDs from Intent
        userId = intent.getStringExtra("userId")
        existingSalonId = intent.getStringExtra("salonId")

        // Initialize all UI components
        initViews()

        // WHY: A dual-mode screen improves owner experience by allowing both creation and editing in one familiar layout.
        // WHAT: We dynamically set the title based on the presence of existingSalonId.
        // HOW: Title is updated to "Edit Salon Details" if salon exists, otherwise "Add Salon Details".
        val tvHeaderTitle = (btnBack.parent as LinearLayout).getChildAt(1) as TextView
        if (existingSalonId != null) {
            tvHeaderTitle.text = "Edit Salon Details"
            btnRegisterSalon.text = "Update Salon Details"
            fetchSalonDetails(existingSalonId!!)
        } else {
            tvHeaderTitle.text = "Add Salon Details"
            btnRegisterSalon.text = "Register Salon"
            fetchAndPopulateUserData()
        }

        // Setup Category Spinner
        setupSpinner()

        // Back button click listener
        btnBack.setOnClickListener {
            finish()
        }

        // Register button click listener
        btnRegisterSalon.setOnClickListener {
            registerSalon()
        }
    }

    private fun initViews() {
        // Salon Details
        etSalonName = findViewById(R.id.etSalonName)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        // Owner Info
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)

        // Address
        etStreet = findViewById(R.id.etStreet)
        etCity = findViewById(R.id.etCity)
        etState = findViewById(R.id.etState)
        etZip = findViewById(R.id.etZip)
        etCountry = findViewById(R.id.etCountry)

        // Buttons
        btnBack = findViewById(R.id.btnBack)
        btnRegisterSalon = findViewById(R.id.btnRegisterSalon)
    }

    private fun fetchSalonDetails(salonId: String) {

        salonRef.child(salonId).get().addOnSuccessListener { snapshot ->

            if (snapshot.exists()) {

                // Salon fields
                etSalonName.setText(snapshot.child("salonName").value?.toString() ?: "")
                etDescription.setText(snapshot.child("description").value?.toString() ?: "")
                etPhone.setText(snapshot.child("phone").value?.toString() ?: "")

                // Owner name
                ownerName = snapshot.child("ownerName").value?.toString() ?: ""

                val nameParts = ownerName.split(" ", limit = 2)

                etFirstName.setText(nameParts.getOrElse(0) { "" })
                etLastName.setText(nameParts.getOrElse(1) { "" })

                // 🔥 Fetch email from Users node (correct source)
                val ownerId = snapshot.child("ownerId").value?.toString()

                if (!ownerId.isNullOrEmpty()) {

                    userRef.child(ownerId)
                        .child("email")
                        .get()
                        .addOnSuccessListener { userSnapshot ->

                            val email = userSnapshot.getValue(String::class.java) ?: ""

                            etEmail.setText(email)

                            // lock owner identity fields
                            etFirstName.isEnabled = false
                            etLastName.isEnabled = false
                            etEmail.isEnabled = false
                        }
                }

                // Address
                val address = snapshot.child("address")

                etStreet.setText(address.child("street").value?.toString() ?: "")
                etCity.setText(address.child("city").value?.toString() ?: "")
                etState.setText(address.child("state").value?.toString() ?: "")
                etZip.setText(address.child("zip").value?.toString() ?: "")
                etCountry.setText(address.child("country").value?.toString() ?: "")
            }

        }.addOnFailureListener {

            Toast.makeText(
                this,
                "Failed to load salon details",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fetchAndPopulateUserData() {
        if (userId == null) return

        userRef.child(userId!!).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ownerName = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()

                // Split name into first and last name
                val nameParts = ownerName.split(" ", limit = 2)
                val firstName = nameParts.getOrElse(0) { "" }
                val lastName = nameParts.getOrElse(1) { "" }

                // Auto-fill fields
                etFirstName.setText(firstName)
                etLastName.setText(lastName)
                etEmail.setText(email)

                // Make fields non-editable
                etFirstName.isEnabled = false
                etLastName.isEnabled = false
                etEmail.isEnabled = false
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Hair & Styling", "Nail Art", "Facial & Makeup", "Spa & Massage")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
    }

    private fun registerSalon() {
        // 1. 📌 COLLECT ALL INPUT VALUES
        val salonName = etSalonName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val phone = etPhone.text.toString().trim()
        
        // Address fields
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val state = etState.text.toString().trim()
        val zip = etZip.text.toString().trim()
        val country = etCountry.text.toString().trim()

        // 2. 📌 VALIDATION
        if (salonName.isEmpty() || description.isEmpty() || phone.isEmpty() ||
            street.isEmpty() || city.isEmpty() || country.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null && existingSalonId == null) return

        // WHY: The salonId must remain constant during updates to ensure data integrity (bookings, etc.) is preserved.
        // WHAT: We use updateChildren() to only modify the specified fields while keeping others like createdAt intact.
        // HOW: updateChildren() is used for existing IDs, whereas push() is only used to generate brand new IDs.
        val finalSalonId = existingSalonId ?: salonRef.push().key ?: return

        val salonData = HashMap<String, Any>()
        salonData["salonId"] = finalSalonId
        salonData["ownerId"] = userId ?: ""
        salonData["ownerName"] = ownerName
        salonData["salonName"] = salonName
        salonData["description"] = description
        salonData["category"] = category
        salonData["phone"] = phone
        
        // Address Object
        val addressMap = HashMap<String, String>()
        addressMap["street"] = street
        addressMap["city"] = city
        addressMap["state"] = state
        addressMap["zip"] = zip
        addressMap["country"] = country
        salonData["address"] = addressMap

        // Update mode logic
        if (existingSalonId != null) {
            salonRef.child(finalSalonId).updateChildren(salonData).addOnSuccessListener {
                Toast.makeText(this, "Salon details updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Check if already registered (only for new registrations)
            userRef.child(userId!!).child("salonId").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(this, "You already registered a salon", Toast.LENGTH_SHORT).show()
                } else {
                    salonData["status"] = "pending"
                    salonData["createdAt"] = System.currentTimeMillis()
                    salonData["commission"] = "10%" 
                    salonData["payout"] = "UPI"

                    salonRef.child(finalSalonId).setValue(salonData).addOnSuccessListener {
                        userRef.child(userId!!).child("salonId").setValue(finalSalonId)
                        Toast.makeText(this, "Waiting for Admin Approval", Toast.LENGTH_LONG).show()
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
