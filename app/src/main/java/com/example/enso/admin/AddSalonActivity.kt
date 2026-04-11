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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_salon)

        // 1. 📌 INITIALIZE FIREBASE
        database = FirebaseDatabase.getInstance()
        salonRef = database.getReference("Salons")
        userRef = database.getReference("Users")

        // Get userId from Intent
        userId = intent.getStringExtra("userId")

        // Initialize all UI components
        initViews()

        // Fetch and Auto-fill User Data
        fetchAndPopulateUserData()

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

        // 3. 📌 GENERATE UNIQUE SALON ID
        val salonId = salonRef.push().key ?: return

        // 4. 📌 WORKING HOURS DATA
        val workingHours = HashMap<String, String>()
        val dayMap = mapOf(
            "Mon" to R.id.tvMon, "Tue" to R.id.tvTue, "Wed" to R.id.tvWed,
            "Thu" to R.id.tvThu, "Fri" to R.id.tvFri, "Sat" to R.id.tvSat, "Sun" to R.id.tvSun
        )

        for ((day, id) in dayMap) {
            val tvDay = findViewById<TextView>(id)
            val parent = tvDay.parent as RelativeLayout
            val tvTime = parent.getChildAt(1) as TextView
            val sw = parent.getChildAt(2) as SwitchMaterial
            workingHours[day] = if (sw.isChecked) tvTime.text.toString() else "Closed"
        }

        // 5. 📌 PREPARE SALON DATA
        val salonData = HashMap<String, Any>()
        salonData["salonId"] = salonId
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

        salonData["workingHours"] = workingHours
        salonData["status"] = "pending"
        salonData["createdAt"] = System.currentTimeMillis()
        
        // Placeholder for missing UI fields as per requirements
        salonData["commission"] = "10%" 
        salonData["payout"] = "UPI"

        // 6. 📌 SAVE TO FIREBASE: Salons → salonId
        salonRef.child(salonId).setValue(salonData)
            .addOnSuccessListener {
                Toast.makeText(this, "Waiting for Admin Approval", Toast.LENGTH_LONG).show()
                finish() // Go back or to dashboard
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
