package com.example.enso.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminUsersFragment : Fragment() {

    private lateinit var llUserList: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_users_dashboard, container, false)
        llUserList = view.findViewById(R.id.ll_user_list)
        fetchPendingUsers()
        return view
    }

    private fun fetchPendingUsers() {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                llUserList.removeAllViews()
                for (userSnap in snapshot.children) {
                    val role = userSnap.child("role").value.toString()
                    val status = userSnap.child("status").value.toString()

                    if (role == "Salon Owner" && status == "pending") {
                        val name = userSnap.child("name").value.toString()
                        val email = userSnap.child("email").value.toString()
                        val userId = userSnap.key

                        val itemView = layoutInflater.inflate(R.layout.item_user, llUserList, false)
                        itemView.findViewById<TextView>(R.id.tv_name).text = name
                        itemView.findViewById<TextView>(R.id.tv_email).text = email
                        itemView.findViewById<TextView>(R.id.tv_status).text = status
                        
//                        val adminActions = itemView.findViewById<LinearLayout>(R.id.admin_actions)
//                        adminActions.visibility = View.VISIBLE
//
//                        itemView.findViewById<View>(R.id.btn_accept).setOnClickListener {
//                            val updates = HashMap<String, Any>()
//                            updates["status"] = "approved"
//                            database.child(userId!!).updateChildren(updates)
//                        }
//
//                        itemView.findViewById<View>(R.id.btn_reject).setOnClickListener {
//                            val updates = HashMap<String, Any>()
//                            updates["status"] = "rejected"
//                            database.child(userId!!).updateChildren(updates)
//                        }

                        llUserList.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
