package com.dankanq.rickandmorty.presentation.character.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.setFragmentResult
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.databinding.FragmentFilterCharacterListBinding
import com.dankanq.rickandmorty.utils.presentation.onDone
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FilterCharacterListFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterCharacterListBinding? = null
    private val binding: FragmentFilterCharacterListBinding
        get() = _binding ?: throw RuntimeException("FragmentFilterCharacterListBinding is null")

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val topPartView = bottomSheet.findViewById<View>(R.id.cl_filter_characters)
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
        _binding = FragmentFilterCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputs()
        setupGenderList()
        setupSearchButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupInputs() {
        with(binding) {
            etName.onDone { etName.clearFocus() }
            etType.onDone { etType.clearFocus() }
        }
    }

    private fun setupSearchButton() {
        binding.bSearch.setOnClickListener {
            saveSearchParams()
        }
    }

    private fun saveSearchParams() {
        val name: String? = binding.etName.text?.takeIf { it.isNotEmpty() }?.toString()

        val checkedStatusButtonIds: List<Int> = binding.btgStatus.checkedButtonIds
        val status: String? = if (checkedStatusButtonIds.isNotEmpty()) {
            val checkedButtonId = checkedStatusButtonIds[0]
            val checkedButton: MaterialButton = binding.btgStatus.findViewById(checkedButtonId)
            checkedButton.text.toString()
        } else {
            null
        }

        val species = binding.etSpecies.text?.takeIf { it.isNotEmpty() }?.toString()

        val type = binding.etType.text?.takeIf { it.isNotEmpty() }?.toString()

        val genderFromSpinner = binding.gender.text.toString()
        val gender = if (genderFromSpinner == "Any") ""
        else genderFromSpinner

        val bundle = Bundle().apply {
            putString(BUNDLE_NAME_KEY, name)
            putString(BUNDLE_STATUS_KEY, status)
            putString(BUNDLE_SPECIES_KEY, species)
            putString(BUNDLE_TYPE_KEY, type)
            putString(BUNDLE_GENDER_KEY, gender)
        }

        setFragmentResult(SEARCH_CHARACTERS_RESULT_KEY, bundle)

        dismiss()
    }

    private fun setupGenderList() {
        val genderArray = resources.getStringArray(R.array.genders)
        val genderListAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_gender,
            genderArray
        )
        binding.gender.apply {
            setText(genderArray[0])
            setAdapter(genderListAdapter)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.background_gender,
                    null
                )
            )
        }
    }

    companion object {
        const val TAG = "search_characters_tag"

        const val SEARCH_CHARACTERS_RESULT_KEY = "search_characters_result"
        const val BUNDLE_NAME_KEY = "bundle_name"
        const val BUNDLE_STATUS_KEY = "bundle_status"
        const val BUNDLE_SPECIES_KEY = "bundle_species"
        const val BUNDLE_TYPE_KEY = "bundle_type"
        const val BUNDLE_GENDER_KEY = "bundle_gender"
    }
}