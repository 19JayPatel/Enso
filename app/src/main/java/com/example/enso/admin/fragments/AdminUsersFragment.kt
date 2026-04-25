package com.example.enso.admin.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.admin.models.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminUsersFragment : Fragment() {

    private lateinit var llUserList: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvActive: TextView
    private lateinit var tvInactive: TextView
    private lateinit var tvBanned: TextView
    private lateinit var tvTotalMembers: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_users_dashboard, container, false)

        llUserList = view.findViewById(R.id.ll_user_list)
        tvTotal = view.findViewById(R.id.tv_summary_total)
        tvActive = view.findViewById(R.id.tv_summary_active)
        tvInactive = view.findViewById(R.id.tv_summary_inactive)
        tvBanned = view.findViewById(R.id.tv_summary_banned)
        tvTotalMembers = view.findViewById(R.id.tv_total_members)

        fetchUsers()
        return view
    }

    private fun fetchUsers() {
        val database = FirebaseDatabase.getInstance().getReference("Users")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                llUserList.removeAllViews()

                var total = 0
                var active = 0
                var inactive = 0
                var banned = 0

                for (data in snapshot.children) {
                    val user = data.getValue(UserModel::class.java)

                    if (user != null) {
                        // 🔥 DEBUG
                        Log.d("DEBUG_ROLE", user.role)

                        // 🔥 FORCE include BOTH roles
                        if (user.role == "customer" || user.role == "salon_owner") {

                            total++

                            when (user.status) {
                                "active" -> active++
                                "inactive" -> inactive++
                                "banned" -> banned++
                            }

                            // Inflate and add user item to list (UI mapping)
                            val itemView = layoutInflater.inflate(R.layout.item_user, llUserList, false)

                            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                            val tvEmail = itemView.findViewById<TextView>(R.id.tv_email)
                            val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)
                            val tvInitials = itemView.findViewById<TextView>(R.id.tv_initials)

                            // Set role-based name display
                            if (user.role == "salon_owner") {
                                tvName.text = "${user.name} (Salon Owner)"
                            } else {
                                tvName.text = "${user.name} (Customer)"
                            }

                            tvEmail.text = user.email
                            tvStatus.text = user.status.replaceFirstChar { it.uppercase() }

                            // Set initials
                            tvInitials.text = if (user.name.isNotEmpty()) user.name.take(2).uppercase() else "U"

                            // Status UI styling
                            when (user.status) {
                                "active" -> tvStatus.backgroundTintList = ColorStateList.valueOf(0xFFE7FDF0.toInt())
                                "banned" -> tvStatus.backgroundTintList = ColorStateList.valueOf(0xFFFEECEB.toInt())
                                "inactive" -> tvStatus.backgroundTintList = ColorStateList.valueOf(0xFFF5F5F5.toInt())
                            }

                            llUserList.addView(itemView)
                        }
                    }
                }

                // Update UI counts
                tvTotal.text = total.toString()
                tvActive.text = active.toString()
                tvInactive.text = inactive.toString()
                tvBanned.text = banned.toString()
                tvTotalMembers.text = "$total members"
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}