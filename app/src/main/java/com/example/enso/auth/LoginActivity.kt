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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.enso.customer.activities.MainActivity
import com.example.enso.R
import com.example.enso.admin.AdminMainActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val emailEt = findViewById<EditText>(R.id.emailEt)
        val passwordEt = findViewById<EditText>(R.id.passwordEt)
        val continueBtn = findViewById<MaterialButton>(R.id.continueBtn)

        continueBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            // 1. Check if email and password are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 🔥 FIX LOGIN FLOW: Use signInWithEmailAndPassword
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // 3. Fetch user role from Realtime Database
                            fetchUserRoleAndNavigate(userId)
                        }
                    } else {
                        // 4. Show Toast with error message if login fails
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        setupSignupRedirect()
    }

    private fun fetchUserRoleAndNavigate(userId: String) {
        // Fetch data from "Users" node
        database.getReference("Users").child(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot != null && snapshot.exists()) {
                        val role = snapshot.child("role").value.toString()
                        val status = snapshot.child("status").value.toString()

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        when (role) {
                            "admin" -> {
                                val intent = Intent(this, AdminMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            "customer" -> {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            "owner" -> {
                                if (status == "approved") {
                                    // Owners go to their dashboard (if you have one)
                                    // For now using AdminMainActivity as placeholder or check if OwnerMainActivity exists
                                    val intent = Intent(this, AdminMainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    auth.signOut()
                                    Toast.makeText(
                                        this,
                                        "Waiting for admin approval",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            else -> {
                                Toast.makeText(this, "Invalid role: $role", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Database Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setupSignupRedirect() {
        val loginRedirect = findViewById<TextView>(R.id.loginRedirect)
        val fullText = "Don’t have an account? Sign up"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Sign up")
        val endIndex = fullText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
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