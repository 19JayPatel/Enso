package com.example.enso.customer.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId: String? = null

    // UI Elements
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etDob: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSaveChanges: Button

    private var selectedGender: String = "Other" // Default value

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid

        if (userId == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return
        }

        // Initialize Views using IDs
        etFirstName = view.findViewById(R.id.et_first_name)
        etLastName = view.findViewById(R.id.et_last_name)
        etEmail = view.findViewById(R.id.et_email)
        etPhone = view.findViewById(R.id.et_phone)
        etDob = view.findViewById(R.id.et_dob)
        etLocation = view.findViewById(R.id.et_location)
        btnSaveChanges = view.findViewById(R.id.btn_save_changes)

        val btnFemale = view.findViewById<TextView>(R.id.btn_female)
        val btnMale = view.findViewById<TextView>(R.id.btn_male)
        val btnOther = view.findViewById<TextView>(R.id.btn_other)

        // 🔥 1. LOAD ALL DATA FROM FIREBASE (Ensures fields are not blank)
        loadUserData(btnFemale, btnMale, btnOther)

        // Back button
        view.findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // DOB Picker
        etDob.setOnClickListener { showDatePicker() }

        // Gender clicks
        btnFemale.setOnClickListener { updateGenderUI("Female", btnFemale, btnMale, btnOther) }
        btnMale.setOnClickListener { updateGenderUI("Male", btnFemale, btnMale, btnOther) }
        btnOther.setOnClickListener { updateGenderUI("Other", btnFemale, btnMale, btnOther) }

        // 🔥 2. SAVE ALL DATA ON BUTTON CLICK
        btnSaveChanges.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun loadUserData(f: TextView, m: TextView, o: TextView) {
        database.getReference("Users").child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fullName = snapshot.child("name").value?.toString() ?: ""
                        val email = snapshot.child("email").value?.toString() ?: ""
                        val phone = snapshot.child("phone").value?.toString() ?: ""
                        val dob = snapshot.child("dob").value?.toString() ?: ""
                        val location = snapshot.child("location").value?.toString() ?: ""
                        val gender = snapshot.child("gender").value?.toString() ?: "Other"

                        // Split Name into First and Last
                        val nameParts = fullName.split(" ")
                        etFirstName.setText(nameParts.getOrNull(0) ?: "")
                        etLastName.setText(
                            if (nameParts.size > 1) nameParts.subList(
                                1,
                                nameParts.size
                            ).joinToString(" ") else ""
                        )

                        etEmail.setText(email)
                        etPhone.setText(phone)
                        etDob.setText(dob)
                        etLocation.setText(location)

                        // Select correct gender button
                        updateGenderUI(gender, f, m, o)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateGenderUI(gender: String, f: TextView, m: TextView, o: TextView) {
        selectedGender = gender
        f.isSelected = (gender == "Female")
        m.isSelected = (gender == "Male")
        o.isSelected = (gender == "Other")
    }

    private fun updateUserProfile() {
        val fName = etFirstName.text.toString().trim()
        val lName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val dob = etDob.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty()) {
            Toast.makeText(context, "Name and Email are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Map all data to save
        val updates = HashMap<String, Any>()
        updates["name"] = "$fName $lName"
        updates["email"] = email
        updates["phone"] = phone
        updates["dob"] = dob
        updates["gender"] = selectedGender
        updates["location"] = location

        database.getReference("Users").child(userId!!).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT)
                        .show()
                    parentFragmentManager.popBackStack() // Go back to Profile
                } else {
                    Toast.makeText(
                        context,
                        "Update failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val months = arrayOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )
                etDob.setText("${months[month]} $day, $year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}