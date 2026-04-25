package com.example.enso.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.auth.LoginActivity
import com.example.enso.customer.activities.MainActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // UI Elements
        val tvUserName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvProfileInitial = view.findViewById<TextView>(R.id.tv_profile_initial)
        val btnLogout = view.findViewById<AppCompatButton>(R.id.btn_logout)
        val btnBack = view.findViewById<MaterialCardView>(R.id.btnBack)
        val editProfileSection = view.findViewById<LinearLayout>(R.id.item_edit_profile)

        // 🔥 1. SESSION CHECK
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }

        // 🔥 2. FETCH DATA FROM FIREBASE
        val userId = currentUser.uid
        database.getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get name and email
                        val name = snapshot.child("name").value.toString()
                        val email = snapshot.child("email").value.toString()

                        // Update UI
                        tvUserName.text = name
                        tvUserEmail.text = email

                        // 🔥 Set the first letter as initial
                        if (name.isNotEmpty()) {
                            val firstLetter = name.substring(0, 1).uppercase()
                            tvProfileInitial.text = firstLetter
                        }
                    } else {
                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // 🔥 3. LOGOUT LOGIC
        btnLogout?.setOnClickListener {
            auth.signOut()
            redirectToLogin()
        }

        // Back button to go to Home
        btnBack?.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.bottomNav?.selectedItemId = MainActivity.NAV_HOME
        }

        // Edit profile section
        editProfileSection?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}
