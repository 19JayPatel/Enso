package com.example.enso.customer.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.enso.R
import java.util.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var selectedGender: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the back button click to return to the previous fragment
        val ivBack = view.findViewById<ImageView>(R.id.iv_back)
        ivBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Date of Birth Calendar Picker
        val etDob = view.findViewById<EditText>(R.id.et_dob)
        etDob.setOnClickListener {
            showDatePicker(etDob)
        }

        // Gender Selection
        val btnFemale = view.findViewById<TextView>(R.id.btn_female)
        val btnMale = view.findViewById<TextView>(R.id.btn_male)
        val btnOther = view.findViewById<TextView>(R.id.btn_other)

        // Set "Other" as default selected to match image
        selectGender(btnOther)

        btnFemale.setOnClickListener { selectGender(btnFemale) }
        btnMale.setOnClickListener { selectGender(btnMale) }
        btnOther.setOnClickListener { selectGender(btnOther) }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val months = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                editText.setText("${months[selectedMonth]} $selectedDay, $selectedYear")
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun selectGender(textView: TextView) {
        selectedGender?.isSelected = false
        textView.isSelected = true
        selectedGender = textView
    }
}