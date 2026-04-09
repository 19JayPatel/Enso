package com.example.enso.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        // Reference to "Users" node in Realtime Database
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Get Views
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val rgUserRole = findViewById<RadioGroup>(R.id.rgUserRole)
        val btnContinue = findViewById<View>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val selectedRoleId = rgUserRole.checkedRadioButtonId

            // 1. Validation: Check if fields are empty
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Create User in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val userId = auth.currentUser!!.uid

                        // Determine role and status
                        val role = if (selectedRoleId == R.id.rbCustomer) "customer" else "owner"
                        val status = if (role == "customer") "active" else "pending"

                        // Prepare User Data (🔥 Added default fields for Edit Profile)
                        val userObject = HashMap<String, Any>()
                        userObject["name"] = name
                        userObject["email"] = email
                        userObject["role"] = role
                        userObject["status"] = status
                        userObject["phone"] = ""
                        userObject["dob"] = ""
                        userObject["gender"] = ""
                        userObject["location"] = ""
                        userObject["createdAt"] = System.currentTimeMillis()

                        // 3. Save user data in Realtime Database
                        database.child(userId).setValue(userObject)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {

                                    if (role == "customer") {
                                        // Navigate to MainActivity for customers
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Navigate to Log in for owners (waiting for approval)
                                        Toast.makeText(
                                            this,
                                            "Waiting for admin approval",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Database Error: ${dbTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    } else {
                        // Show error message if task fails
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Setup "Sign In" link redirect
        setupLoginRedirect()
    }

    private fun setupLoginRedirect() {
        val loginRedirect = findViewById<TextView>(R.id.loginRedirect)
        val fullText = "Already have an account? Sign In"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Sign In")
        val endIndex = fullText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        spannable.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            ForegroundColorSpan("#A46C54".toColorInt()),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        loginRedirect.text = spannable
        loginRedirect.movementMethod = LinkMovementMethod.getInstance()
        loginRedirect.highlightColor = Color.TRANSPARENT
    }
}