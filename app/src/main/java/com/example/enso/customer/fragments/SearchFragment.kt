package com.example.enso.customer.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.example.enso.R

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        
        // Auto-focus and show keyboard
        etSearch.requestFocus()
        etSearch.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        // Simulating "Recent History" state to visualize the list
        val hasSearchHistory = true
        
        val llEmptyState = view.findViewById<LinearLayout>(R.id.llEmptyState)
        val groupRecentSearches = view.findViewById<Group>(R.id.groupRecentSearches)

        if (hasSearchHistory) {
            llEmptyState.visibility = View.GONE
            groupRecentSearches.visibility = View.VISIBLE
            setupRecentSearches(view)
        } else {
            llEmptyState.visibility = View.VISIBLE
            groupRecentSearches.visibility = View.GONE
        }
    }

    private fun setupRecentSearches(view: View) {
        setupSearchItem(view.findViewById(R.id.item1), R.string.loc_1_title, R.string.loc_1_sub)
        setupSearchItem(view.findViewById(R.id.item2), R.string.loc_2_title, R.string.loc_2_sub)
        setupSearchItem(view.findViewById(R.id.item3), R.string.loc_3_title, R.string.loc_3_sub)
        setupSearchItem(view.findViewById(R.id.item4), R.string.loc_4_title, R.string.loc_4_sub)
        setupSearchItem(view.findViewById(R.id.item5), R.string.loc_5_title, R.string.loc_5_sub)
        setupSearchItem(view.findViewById(R.id.item6), R.string.loc_6_title, R.string.loc_6_sub)
    }

    private fun setupSearchItem(view: View, titleRes: Int, subRes: Int) {
        view.findViewById<TextView>(R.id.tvSearchTitle).setText(titleRes)
        view.findViewById<TextView>(R.id.tvSearchSubtitle).setText(subRes)
    }
}