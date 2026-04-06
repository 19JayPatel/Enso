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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_salon)

        // 1. 📌 INITIALIZE FIREBASE
        database = FirebaseDatabase.getInstance()
        salonRef = database.getReference("Salons")

        // Initialize all UI components
        initViews()

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

    private fun setupSpinner() {
        val categories = arrayOf("Hair & Styling", "Nail Art", "Facial & Makeup", "Spa & Massage")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
    }

    private fun registerSalon() {
        // 3. 📌 GET ALL INPUT VALUES
        val salonName = etSalonName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val state = etState.text.toString().trim()
        val zip = etZip.text.toString().trim()
        val country = etCountry.text.toString().trim()

        // 5. 📌 VALIDATION
        if (salonName.isEmpty() || description.isEmpty() || firstName.isEmpty() ||
            lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            street.isEmpty() || city.isEmpty() || country.isEmpty()
        ) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 📌 GENERATE UNIQUE SALON ID
        val salonId = salonRef.push().key!!

        // 4. 📌 WORKING HOURS DATA
        // Note: Accessing switches by hierarchy as they don't have IDs in XML
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

        // 6. 📌 SAVE DATA TO FIREBASE (Prepare HashMap)
        val salonData = HashMap<String, Any>()
        salonData["salonId"] = salonId
        salonData["salonName"] = salonName
        salonData["description"] = description
        salonData["category"] = category
        salonData["ownerFirstName"] = firstName
        salonData["ownerLastName"] = lastName
        salonData["email"] = email
        salonData["phone"] = phone
        salonData["street"] = street
        salonData["city"] = city
        salonData["state"] = state
        salonData["zip"] = zip
        salonData["country"] = country
        salonData["workingHours"] = workingHours
        salonData["createdAt"] = System.currentTimeMillis()

        // 7. 📌 INSERT INTO DATABASE
        salonRef.child(salonId).setValue(salonData)
            .addOnSuccessListener {
                // 8. 📌 SUCCESS HANDLING
                Toast.makeText(this, "Salon Registered Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                // 9. 📌 ERROR HANDLING
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
