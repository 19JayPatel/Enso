package com.example.enso.customer.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.ChooseStylistActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox

class SalonDetailsFragment : Fragment() {

    private val selectedServices = mutableSetOf<Int>()
    private lateinit var btnContinue: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_salon_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnContinue = view.findViewById(R.id.btnContinue)
        
        btnContinue.setOnClickListener {
            val intent = Intent(requireContext(), ChooseStylistActivity::class.java)
            startActivity(intent)
        }

        // Initialize all service clicks
        setupServiceClick(view, R.id.cvService1, R.id.cbService1, 1)
        setupServiceClick(view, R.id.cvService2, R.id.cbService2, 2)
        setupServiceClick(view, R.id.cvService3, R.id.cbService3, 3)
        setupServiceClick(view, R.id.cvService4, R.id.cbService4, 4)
        setupServiceClick(view, R.id.cvService5, R.id.cbService5, 5)
        setupServiceClick(view, R.id.cvService6, R.id.cbService6, 6)
        setupServiceClick(view, R.id.cvService7, R.id.cbService7, 7)

        updateContinueButton()
    }

    private fun setupServiceClick(root: View, cardId: Int, checkBoxId: Int, serviceId: Int) {
        val card = root.findViewById<MaterialCardView>(cardId)
        val checkBox = root.findViewById<MaterialCheckBox>(checkBoxId)

        card.setOnClickListener {
            val isCurrentlyChecked = card.isChecked
            val newState = !isCurrentlyChecked
            
            card.isChecked = newState
            checkBox.isChecked = newState

            if (newState) {
                selectedServices.add(serviceId)
                card.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_brown)))
                card.setStrokeWidth(4)
            } else {
                selectedServices.remove(serviceId)
                card.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT))
                card.setStrokeWidth(0)
            }
            updateContinueButton()
        }
    }

    private fun updateContinueButton() {
        val count = selectedServices.size
        btnContinue.text = "Continue ($count)"
        
        if (count > 0) {
            btnContinue.isEnabled = true
            btnContinue.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_brown))
            btnContinue.setTextColor(Color.WHITE)
        } else {
            btnContinue.isEnabled = false
            btnContinue.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D3D3D3")) // Grey
            btnContinue.setTextColor(Color.parseColor("#8E8E8E")) // Light grey text
        }
    }
}
