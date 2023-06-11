package com.dankanq.rickandmorty.presentation.character.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.RickAndMortyApp
import com.dankanq.rickandmorty.databinding.FragmentCharactersBinding
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.BUNDLE_GENDER_KEY
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.BUNDLE_NAME_KEY
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.BUNDLE_SPECIES_KEY
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.BUNDLE_STATUS_KEY
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.BUNDLE_TYPE_KEY
import com.dankanq.rickandmorty.presentation.character.main.FilterCharactersFragment.Companion.SEARCH_CHARACTERS_RESULT_KEY
import com.dankanq.rickandmorty.presentation.character.main.adapter.CharactersAdapter
import com.dankanq.rickandmorty.utils.presentation.addTextChangedListener
import com.dankanq.rickandmorty.utils.presentation.onSearch
import com.dankanq.rickandmorty.utils.presentation.simpleScan
import com.dankanq.rickandmorty.utils.presentation.MainLoadStateAdapter
import com.dankanq.rickandmorty.utils.presentation.TryAgainAction
import com.dankanq.rickandmorty.utils.presentation.ViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CharactersFragment : Fragment() {
    private var _binding: FragmentCharactersBinding? = null
    private val binding: FragmentCharactersBinding
        get() = _binding ?: throw RuntimeException("FragmentCharactersBinding is null")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[CharactersViewModel::class.java]
    }

    private val component by lazy {
        (requireActivity().application as RickAndMortyApp).component
    }

    private val adapter by lazy {
        CharactersAdapter(requireContext())
    }

    private lateinit var layoutManager: GridLayoutManager
    private lateinit var mainLoadStateViewHolder: MainLoadStateAdapter.MainLoaderViewHolder

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBarLayout()

        setupRecyclerView()
        setupSwipeRefreshLayout()

        setupFilterButton()
        setupFilterFragmentResultListener()

        observeCharacters()
        observeLoadState()

//        handleListVisibility()
        handleScrollingToTop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAppBarLayout() {
        with(binding) {
            etSearch.apply {
                addTextChangedListener(bSearch, bClear)
                onSearch {
                    val name = etSearch.text.toString()
                    viewModel.applyFilters(
                        params = CharactersViewModel.FilterParams(
                            name = name
                        )
                    )
                    clearFocus()
                }
            }
            bSearch.setOnClickListener {
                etSearch.requestFocus()
            }
            bClear.setOnClickListener {
                etSearch.text.clear()
                bClear.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        adapter.onCharacterClick = { character ->
            val fragment = CharacterFragment.newInstance(character.id)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
        val tryAgainAction: TryAgainAction = { adapter.retry() }
        val footerAdapter = MainLoadStateAdapter(tryAgainAction)
        val adapterWithLoadState = adapter.withLoadStateFooter(footerAdapter)
        binding.recyclerView.adapter = adapterWithLoadState
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == adapter.itemCount && footerAdapter.itemCount > 0) {
                    2
                } else {
                    1
                }
            }
        }
        mainLoadStateViewHolder = MainLoadStateAdapter.MainLoaderViewHolder(
            binding.loadState,
            tryAgainAction
        )

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (binding.fabFilter.isShown) {
                        binding.fabFilter.hide()
                    }
                } else if (dy < 0) {
                    if (binding.appBarLayout.isShown && !binding.fabFilter.isShown) {
                        binding.fabFilter.show()
                    }
                }
            }
        })
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun setupFilterButton() {
        binding.fabFilter.setOnClickListener {
            FilterCharactersFragment().show(
                requireActivity().supportFragmentManager,
                FilterCharactersFragment.TAG
            )
        }
    }

    private fun setupFilterFragmentResultListener() {
        setFragmentResultListener(SEARCH_CHARACTERS_RESULT_KEY) { _, bundle ->
            val name = bundle.getString(BUNDLE_NAME_KEY).orEmpty()
            val status = bundle.getString(BUNDLE_STATUS_KEY).orEmpty()
            val species = bundle.getString(BUNDLE_SPECIES_KEY).orEmpty()
            val type = bundle.getString(BUNDLE_TYPE_KEY).orEmpty()
            val gender = bundle.getString(BUNDLE_GENDER_KEY).orEmpty()

            val filterParams = CharactersViewModel.FilterParams(
                name = name,
                status = status,
                species = species,
                type = type,
                gender = gender
            )
            viewModel.applyFilters(filterParams)
        }
    }

    private fun observeCharacters() {
        lifecycleScope.launch {
            viewModel.charactersFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeLoadState() {
        adapter.loadStateFlow
            .debounce(500)
            .onEach {
                mainLoadStateViewHolder.bind(it.refresh)
            }
            .launchIn(lifecycleScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleListVisibility() = lifecycleScope.launch {
        getRefreshLoadStateFlow(adapter)
            .simpleScan(count = 3)
            .collectLatest { (beforePrevious, previous, current) ->
                binding.recyclerView.isInvisible = current is LoadState.Error
                        || previous is LoadState.Error
                        || (beforePrevious is LoadState.Error
                        && previous is LoadState.NotLoading
                        && current is LoadState.Loading)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleScrollingToTop() = lifecycleScope.launch {
        getRefreshLoadStateFlow(adapter)
            .simpleScan(count = 3)
            .collect { (previousState, currentState) ->
                if (previousState is LoadState.Loading
                    && currentState is LoadState.NotLoading
                ) {
                    delay(200)
                    binding.recyclerView.scrollToPosition(0)
                }
            }
    }

    private fun getRefreshLoadStateFlow(adapter: CharactersAdapter): Flow<LoadState> {
        return adapter.loadStateFlow.map { it.refresh }
    }

    companion object {
        fun newInstance() = CharactersFragment()
    }
}