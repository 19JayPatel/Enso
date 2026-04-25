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
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.enso.R
import com.example.enso.admin.activities.AddSalonActivity
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
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Get Views
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val rbCustomer = findViewById<RadioButton>(R.id.rbCustomer)
        val rbOwner = findViewById<RadioButton>(R.id.rbOwner)
        val btnContinue = findViewById<View>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // STEP 1: GET SELECTED ROLE
            val role = when {
                rbCustomer.isChecked -> "customer"
                rbOwner.isChecked -> "salon_owner"
                else -> ""
            }

            // STEP 2: VALIDATION
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role.isEmpty()) {
                Toast.makeText(this, "Please select role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // STEP 3: FIREBASE AUTH
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // STEP 4: SAVE USER DATA
                        val userMap = HashMap<String, Any>()
                        userMap["name"] = name
                        userMap["email"] = email
                        userMap["role"] = role
                        
                        // WHY: Salon owners must start as 'pending' to allow admins to verify their credentials before they can interact with customers.
                        // WHAT: This logic checks the user's role and assigns 'pending' status to owners while customers get 'active' status immediately.
                        // HOW: By defaulting owners to 'pending', the system prevents unauthorized dashboard access and enforces a mandatory admin approval workflow for all service providers.
                        if (role == "salon_owner") {
                            userMap["status"] = "pending"
                        } else {
                            userMap["status"] = "active"
                        }

                        userMap["phone"] = ""
                        userMap["dob"] = ""
                        userMap["gender"] = ""
                        userMap["location"] = ""
                        userMap["createdAt"] = System.currentTimeMillis()

                        database.child(userId).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()

                                    // STEP 5: REDIRECT BASED ON ROLE
                                    if (role == "salon_owner") {
                                        // Owner → Add Salon Flow
                                        val intent = Intent(this, AddSalonActivity::class.java)
                                        intent.putExtra("userId", userId)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Customer → Main App
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // STEP 6: ERROR HANDLING
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

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
