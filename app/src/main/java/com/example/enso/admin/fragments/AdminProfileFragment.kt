package com.example.enso.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.enso.auth.LoginActivity
import com.example.enso.databinding.FragmentAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminProfileFragment : Fragment() {

    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. CHECK ADMIN SESSION
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Redirect to LoginActivity if user is not logged in
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        // 2. FETCH ADMIN EMAIL FROM FIREBASE REALTIME DATABASE
        val userId = user.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Get email and name fields from database
                val email = snapshot.child("email").value.toString()
                val name = snapshot.child("name").value.toString()

                // Set data into TextViews
                binding.tvEmail.text = email
                binding.tvName.text = name
            }
        }.addOnFailureListener {
            // Simple toast if data fetch fails
            Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
        }

        // 3. HANDLE LOGOUT BUTTON
        binding.btnLogout.setOnClickListener {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Redirect to LoginActivity and clear back stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear binding to avoid memory leaks
        _binding = null
    }
}