package com.dankanq.rickandmorty.presentation.location.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.databinding.FragmentFilterLocationListBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterLocationListFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterLocationListBinding? = null
    private val binding: FragmentFilterLocationListBinding
        get() = _binding ?: throw RuntimeException("FragmentFilterLocationListBinding is null")

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val topPartView = bottomSheet.findViewById<View>(R.id.cl_filter_locations)
                topPartView.measure(
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
                val topPartHeight = topPartView.measuredHeight
                behavior.peekHeight = topPartHeight
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterLocationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupSearchButton() {
        binding.bSearch.setOnClickListener {
            saveSearchParams()
        }
    }

    private fun saveSearchParams() {
        val name: String? = binding.etName.text?.takeIf { it.isNotEmpty() }?.toString()
        val type: String? = binding.etType.text?.takeIf { it.isNotEmpty() }?.toString()
        val dimension: String? = binding.etDimension.text?.takeIf { it.isNotEmpty() }?.toString()

        val bundle = Bundle().apply {
            putString(BUNDLE_NAME_KEY, name)
            putString(BUNDLE_TYPE_KEY, type)
            putString(BUNDLE_DIMENSION_KEY, dimension)
        }

        setFragmentResult(SEARCH_LOCATION_LIST_RESULT_KEY, bundle)

        dismiss()
    }

    companion object {
        const val TAG = "search_location_list_tag"

        const val SEARCH_LOCATION_LIST_RESULT_KEY = "search_location_list_result"
        const val BUNDLE_NAME_KEY = "bundle_name"
        const val BUNDLE_TYPE_KEY = "bundle_type"
        const val BUNDLE_DIMENSION_KEY = "bundle_dimension"
    }
}