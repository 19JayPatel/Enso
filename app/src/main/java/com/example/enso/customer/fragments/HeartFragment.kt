package com.example.enso.customer.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

class HeartFragment : Fragment(R.layout.fragment_heart) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.bottomNav.selectedItemId = MainActivity.NAV_HOME
            }
        }

        setupRemoveButtons(view)
    }

    private fun setupRemoveButtons(view: View) {
        val removeButtons = listOf(
            R.id.btnRemove1,
            R.id.btnRemove2,
            R.id.btnRemove3,
            R.id.btnRemove4
        )

        removeButtons.forEach { id ->
            view.findViewById<View>(id)?.setOnClickListener {
                showRemoveDialog()
            }
        }
    }

    private fun showRemoveDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val dialogView = layoutInflater.inflate(R.layout.dialog_remove_favorite, null)
        dialog.setContentView(dialogView)

        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmRemove)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelRemove)

        btnConfirm.setOnClickListener {
            // Logic to remove from favorites would go here
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
