package com.dankanq.rickandmorty.presentation.episode.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.databinding.FragmentFilterEpisodeListBinding
import com.dankanq.rickandmorty.utils.presentation.onDone
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterEpisodeListFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterEpisodeListBinding? = null
    private val binding: FragmentFilterEpisodeListBinding
        get() = _binding ?: throw RuntimeException("FragmentFilterEpisodeListBinding is null")

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val topPartView = bottomSheet.findViewById<View>(R.id.cl_filter_episodes)
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
        _binding = FragmentFilterEpisodeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputs()
        setupSearchButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupInputs() {
        with(binding) {
            etName.onDone { etName.clearFocus() }
            etEpisode.onDone { etEpisode.clearFocus() }
        }
    }

    private fun setupSearchButton() {
        binding.bSearch.setOnClickListener {
            saveSearchParams()
        }
    }

    private fun saveSearchParams() {
        val name: String? = binding.etName.text?.takeIf { it.isNotEmpty() }?.toString()
        val episode: String? = binding.etEpisode.text?.takeIf { it.isNotEmpty() }?.toString()

        val bundle = Bundle().apply {
            putString(BUNDLE_NAME_KEY, name)
            putString(BUNDLE_EPISODE_KEY, episode)
        }

        setFragmentResult(SEARCH_EPISODE_LIST_RESULT_KEY, bundle)

        dismiss()
    }

    companion object {
        const val TAG = "search_episode_list_tag"

        const val SEARCH_EPISODE_LIST_RESULT_KEY = "search_episode_list_result"
        const val BUNDLE_NAME_KEY = "bundle_name"
        const val BUNDLE_EPISODE_KEY = "bundle_episode"
    }
}