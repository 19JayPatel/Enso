package com.example.enso.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.enso.R

class AddSalonActivity : AppCompatActivity() {

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
            validateAndRegister()
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

    private fun validateAndRegister() {
        // Get values from fields
        val salonName = etSalonName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val country = etCountry.text.toString().trim()

        // Basic validation for required fields
        if (salonName.isEmpty() || description.isEmpty() || firstName.isEmpty() || 
            lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || 
            street.isEmpty() || city.isEmpty() || country.isEmpty()) {
            
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            // If all valid, show success message
            Toast.makeText(this, "Salon Registered Successfully", Toast.LENGTH_LONG).show()
            
            // Optionally close activity or clear fields
            // finish()
        }
    }
}
