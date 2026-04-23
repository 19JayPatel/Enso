package com.example.enso.customer.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.example.enso.R
import com.example.enso.customer.activities.SearchResultActivity

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

        // Hide Bottom Navigation when Search is active
        val bottomNav = activity?.findViewById<View>(R.id.bottomNavContainer)
        bottomNav?.visibility = View.GONE

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

        // Capture search action from keyboard
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = etSearch.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    // WHY: Passing the keyword via intent allows SearchResultActivity to dynamically filter content based on user input.
                    // WHAT: Capturing the text from EditText and bundling it into an Intent extra named "searchKeyword".
                    // HOW: This approach ensures that the search intent is clear and the data is safely passed to the next activity for Firebase processing.
                    val intent = Intent(requireContext(), SearchResultActivity::class.java)
                    intent.putExtra("searchKeyword", keyword)
                    startActivity(intent)
                }
                true
            } else {
                false
            }
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        // Show Bottom Navigation when Search is closed
        val bottomNav = activity?.findViewById<View>(R.id.bottomNavContainer)
        bottomNav?.visibility = View.VISIBLE
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
