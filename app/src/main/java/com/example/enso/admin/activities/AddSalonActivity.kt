package com.example.enso.admin.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    private var existingSalonId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_salon)

        database = FirebaseDatabase.getInstance()
        salonRef = database.getReference("Salons")
        userRef = database.getReference("Users")

        userId = intent.getStringExtra("userId")
        existingSalonId = intent.getStringExtra("salonId")

        initViews()

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

        setupSpinner()

        btnBack.setOnClickListener {
            finish()
        }

        btnRegisterSalon.setOnClickListener {
            registerSalon()
        }
    }

    private fun initViews() {
        etSalonName = findViewById(R.id.etSalonName)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)

        etStreet = findViewById(R.id.etStreet)
        etCity = findViewById(R.id.etCity)
        etState = findViewById(R.id.etState)
        etZip = findViewById(R.id.etZip)
        etCountry = findViewById(R.id.etCountry)

        btnBack = findViewById(R.id.btnBack)
        btnRegisterSalon = findViewById(R.id.btnRegisterSalon)
    }

    private fun fetchSalonDetails(salonId: String) {
        salonRef.child(salonId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                etSalonName.setText(snapshot.child("salonName").value?.toString() ?: "")
                etDescription.setText(snapshot.child("description").value?.toString() ?: "")
                etPhone.setText(snapshot.child("phone").value?.toString() ?: "")

                ownerName = snapshot.child("ownerName").value?.toString() ?: ""
                val nameParts = ownerName.split(" ", limit = 2)
                etFirstName.setText(nameParts.getOrElse(0) { "" })
                etLastName.setText(nameParts.getOrElse(1) { "" })

                val ownerId = snapshot.child("ownerId").value?.toString()
                if (!ownerId.isNullOrEmpty()) {
                    userRef.child(ownerId).child("email").get().addOnSuccessListener { userSnapshot ->
                        val email = userSnapshot.getValue(String::class.java) ?: ""
                        etEmail.setText(email)
                        etFirstName.isEnabled = false
                        etLastName.isEnabled = false
                        etEmail.isEnabled = false
                    }
                }

                val address = snapshot.child("address")
                etStreet.setText(address.child("street").value?.toString() ?: "")
                etCity.setText(address.child("city").value?.toString() ?: "")
                etState.setText(address.child("state").value?.toString() ?: "")
                etZip.setText(address.child("zip").value?.toString() ?: "")
                etCountry.setText(address.child("country").value?.toString() ?: "")

                // Fetch Working Hours and set Switch states
                val whSnapshot = snapshot.child("workingHours")
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val dayIds = listOf(R.id.tvMon, R.id.tvTue, R.id.tvWed, R.id.tvThu, R.id.tvFri, R.id.tvSat, R.id.tvSun)

                for (i in days.indices) {
                    val status = whSnapshot.child(days[i]).getValue(String::class.java) ?: "Closed"
                    val tvDay = findViewById<TextView>(dayIds[i])
                    val parent = tvDay.parent as RelativeLayout
                    val switch = parent.getChildAt(2) as SwitchMaterial
                    switch.isChecked = status != "Closed"
                }
            }
        }
    }

    private fun fetchAndPopulateUserData() {
        if (userId == null) return
        userRef.child(userId!!).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ownerName = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val nameParts = ownerName.split(" ", limit = 2)
                etFirstName.setText(nameParts.getOrElse(0) { "" })
                etLastName.setText(nameParts.getOrElse(1) { "" })
                etEmail.setText(email)
                etFirstName.isEnabled = false
                etLastName.isEnabled = false
                etEmail.isEnabled = false
            }
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Hair & Styling", "Nail Art", "Facial & Makeup", "Spa & Massage")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
    }

    private fun registerSalon() {
        val salonName = etSalonName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val phone = etPhone.text.toString().trim()

        val street = etStreet.text.toString().trim()
        val cityStr = etCity.text.toString().trim()
        val stateStr = etState.text.toString().trim()
        val zipStr = etZip.text.toString().trim()
        val countryStr = etCountry.text.toString().trim()

        if (salonName.isEmpty() || description.isEmpty() || phone.isEmpty() ||
            street.isEmpty() || cityStr.isEmpty() || countryStr.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null && existingSalonId == null) return
        val finalSalonId = existingSalonId ?: salonRef.push().key ?: return

        val salonData = HashMap<String, Any>()
        salonData["salonId"] = finalSalonId
        salonData["ownerId"] = userId ?: ""
        salonData["ownerName"] = ownerName
        salonData["salonName"] = salonName
        salonData["description"] = description
        salonData["category"] = category
        salonData["phone"] = phone

        val addressMap = HashMap<String, String>()
        addressMap["street"] = street
        addressMap["city"] = cityStr
        addressMap["state"] = stateStr
        addressMap["zip"] = zipStr
        addressMap["country"] = countryStr
        salonData["address"] = addressMap

        // Collect Working Hours
        val workingHoursMap = HashMap<String, String>()
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dayIds = listOf(R.id.tvMon, R.id.tvTue, R.id.tvWed, R.id.tvThu, R.id.tvFri, R.id.tvSat, R.id.tvSun)

        for (i in days.indices) {
            val tvDay = findViewById<TextView>(dayIds[i])
            val parent = tvDay.parent as RelativeLayout
            val timeText = (parent.getChildAt(1) as TextView).text.toString()
            val switch = parent.getChildAt(2) as SwitchMaterial

            workingHoursMap[days[i]] = if (switch.isChecked) timeText else "Closed"
        }
        salonData["workingHours"] = workingHoursMap

        if (existingSalonId != null) {
            salonRef.child(finalSalonId).updateChildren(salonData).addOnSuccessListener {
                Toast.makeText(this, "Salon details updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
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
                    }
                }
            }
        }
    }
}