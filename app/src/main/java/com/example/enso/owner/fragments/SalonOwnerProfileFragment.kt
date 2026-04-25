package com.example.enso.owner.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.admin.activities.AddSalonActivity
import com.example.enso.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SalonOwnerProfileFragment : Fragment() {

    private lateinit var tvOwnerName: TextView
    private lateinit var tvOwnerSubtitle: TextView
    private lateinit var tvOwnerEmail: TextView
    private lateinit var btnLogout: Button

    // WHY: edit mode detection is required to distinguish between creating a new salon and updating an existing one.
    // WHAT: This ID is fetched from the user's private data in Firebase (Users node).
    // HOW: If this ID is present, it signals AddSalonActivity to switch into "Edit Mode" via Intent.
    private var salonId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_salon_owner_profile, container, false)

        // ✅ STEP 1: GET CURRENT USER
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return view
        }

        val userId = user.uid

        // Initialize UI
        tvOwnerName = view.findViewById(R.id.tvOwnerName)
        tvOwnerSubtitle = view.findViewById(R.id.tvOwnerSubtitle)
        tvOwnerEmail = view.findViewById(R.id.tvOwnerEmail)
        btnLogout = view.findViewById(R.id.btnLogout)

        // ✅ PROFILE (OWNER NAME & EMAIL DYNAMIC)
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    // WHAT: We retrieve the "salonId" field which is linked to this specific user.
                    // HOW: This data pre-fetching improves the owner experience by providing immediate access to their existing salon.
                    salonId = snapshot.child("salonId").getValue(String::class.java)

                    tvOwnerName.text = name ?: "Owner Name"
                    tvOwnerEmail.text = email ?: "No Email"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // ✅ SALON NAME DYNAMIC (Subtitle)
        val salonRef = FirebaseDatabase.getInstance().getReference("Salons")
        salonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (salonSnap in snapshot.children) {
                        val ownerId = salonSnap.child("ownerId").getValue(String::class.java)
                        if (ownerId == userId) {
                            val salonName = salonSnap.child("salonName").getValue(String::class.java)
                            tvOwnerSubtitle.text = "${salonName ?: "Salon"} • Owner"
                            break
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // ✅ LOGOUT LOGIC
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    // Logout from Firebase
                    FirebaseAuth.getInstance().signOut()

                    // Redirect to LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)

                    // Clear back stack so user cannot go back
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    // Close current activity
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Navigation to AddSalonActivity
        val cvSalonSection = view.findViewById<ViewGroup>(R.id.cvSalonSection)
        if (cvSalonSection != null) {
            val salonSectionContainer = cvSalonSection.getChildAt(0) as ViewGroup
            val salonDetailsRow = salonSectionContainer.getChildAt(0)
//            salonDetailsRow.setOnClickListener {
//                val intent = Intent(requireContext(), AddSalonActivity::class.java)
//
//                // WHY: Passing the salonId through the Intent tells the next activity which mode to activate.
//                // WHAT: We send the existing salonId (if any) and the current userId.
//                // HOW: This allows the screen to dynamically change its title and load existing data for editing.
//                intent.putExtra("userId", userId)
//                if (salonId != null) {
//                    intent.putExtra("salonId", salonId)
//                }
//
//                startActivity(intent)
//            }

            salonDetailsRow.setOnClickListener {

                // WHY:
                // Sometimes older salon owners already have salon data inside "Salons"
                // but their Users node does NOT yet contain "salonId".
                // This causes Edit screen to open empty instead of loading details.

                // WHAT:
                // If salonId already exists → open Edit Mode immediately.
                // Else → recover salonId automatically from Salons node.

                // HOW:
                // Query Salons where ownerId == current userId
                // then reuse that salonId and store it inside Users node for future use.

                if (salonId != null) {

                    val intent = Intent(requireContext(), AddSalonActivity::class.java)

                    intent.putExtra("userId", userId)
                    intent.putExtra("salonId", salonId)

                    startActivity(intent)

                } else {

                    val salonRef = FirebaseDatabase.getInstance()
                        .getReference("Salons")

                    salonRef.orderByChild("ownerId")
                        .equalTo(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.exists()) {

                                    val salonSnap = snapshot.children.first()

                                    val recoveredSalonId =
                                        salonSnap.child("salonId")
                                            .getValue(String::class.java)

                                    if (recoveredSalonId != null) {

                                        // Save recovered salonId into Users node
                                        FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child(userId)
                                            .child("salonId")
                                            .setValue(recoveredSalonId)

                                        val intent = Intent(
                                            requireContext(),
                                            AddSalonActivity::class.java
                                        )

                                        intent.putExtra("userId", userId)
                                        intent.putExtra("salonId", recoveredSalonId)

                                        startActivity(intent)

                                    } else {

                                        // No salonId found → open Add Mode
                                        val intent = Intent(
                                            requireContext(),
                                            AddSalonActivity::class.java
                                        )

                                        intent.putExtra("userId", userId)

                                        startActivity(intent)
                                    }

                                } else {

                                    // No salon exists → open Add Mode
                                    val intent = Intent(
                                        requireContext(),
                                        AddSalonActivity::class.java
                                    )

                                    intent.putExtra("userId", userId)

                                    startActivity(intent)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
        }

        return view
    }
}