package com.dankanq.rickandmorty.presentation.episode.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.RickAndMortyApp
import com.dankanq.rickandmorty.databinding.FragmentEpisodeListBinding
import com.dankanq.rickandmorty.presentation.episode.detail.EpisodeFragment
import com.dankanq.rickandmorty.presentation.episode.main.FilterEpisodeListFragment.Companion.BUNDLE_EPISODE_KEY
import com.dankanq.rickandmorty.presentation.episode.main.FilterEpisodeListFragment.Companion.BUNDLE_NAME_KEY
import com.dankanq.rickandmorty.presentation.episode.main.FilterEpisodeListFragment.Companion.SEARCH_EPISODE_LIST_RESULT_KEY
import com.dankanq.rickandmorty.presentation.episode.main.adapter.EpisodeListAdapter
import com.dankanq.rickandmorty.utils.presentation.adapter.MainLoadStateAdapter
import com.dankanq.rickandmorty.utils.presentation.adapter.TryAgainAction
import com.dankanq.rickandmorty.utils.presentation.addTextChangedListener
import com.dankanq.rickandmorty.utils.presentation.onSearch
import com.dankanq.rickandmorty.utils.presentation.simpleScan
import com.dankanq.rickandmorty.utils.presentation.viewmodel.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class EpisodeListFragment : Fragment() {
    private var _binding: FragmentEpisodeListBinding? = null
    private val binding: FragmentEpisodeListBinding
        get() = _binding ?: throw RuntimeException("FragmentEpisodeListBinding is null")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[EpisodeListViewModel::class.java]
    }

    private val component by lazy {
        (requireActivity().application as RickAndMortyApp).component
    }

    private val adapter by lazy {
        EpisodeListAdapter()
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
        _binding = FragmentEpisodeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBarLayout()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        setupFilterButton()
        setupFilterFragmentResultListener()

        observeEpisodeList()
        observeLoadState()

        handleScrollingToTop()
    }

    override fun onResume() {
        super.onResume()

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAppBarLayout() {
        with(binding.layoutAppBar) {
            etSearch.apply {
                addTextChangedListener(bClear)
                onSearch {
                    searchEpisodes()
                }
                hint = getString(R.string.search_episodes)
            }
            bSearch.setOnClickListener {
                searchEpisodes()
            }
            bClear.setOnClickListener {
                etSearch.text.clear()
                bClear.visibility = View.GONE
            }
        }
    }

    private fun searchEpisodes() {
        val name = binding.layoutAppBar.etSearch.text.toString()
        viewModel.applyFilters(
            params = EpisodeListViewModel.EpisodeFilterParams(
                name = name
            )
        )
        binding.layoutAppBar.etSearch.clearFocus()
    }

    private fun setupRecyclerView() {
        layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        adapter.onEpisodeClick = { episode ->
            launchEpisodeFragment(episode.id)
        }
        val tryAgainAction: TryAgainAction = {
            adapter.retry()
        }
        val footerAdapter = createMainLoadStateAdapter(tryAgainAction)
        val adapterWithLoadState = createAdapterWithLoadStateFooter(footerAdapter)
        binding.recyclerView.adapter = adapterWithLoadState
        layoutManager.spanSizeLookup = createSpanSizeLookup(footerAdapter)
        mainLoadStateViewHolder = MainLoadStateAdapter.MainLoaderViewHolder(
            requireContext(),
            binding.mainLoadState,
            binding.swipeRefreshLayout,
            tryAgainAction
        )

        binding.recyclerView.addOnScrollListener(createScrollListener())
    }

    private fun launchEpisodeFragment(episodeId: Long) {
        val fragment = EpisodeFragment.newInstance(episodeId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun createMainLoadStateAdapter(tryAgainAction: TryAgainAction): MainLoadStateAdapter {
        return MainLoadStateAdapter(requireContext(), tryAgainAction)
    }

    private fun createAdapterWithLoadStateFooter(footerAdapter: MainLoadStateAdapter): RecyclerView.Adapter<*> {
        return adapter.withLoadStateFooter(footerAdapter)
    }

    private fun createSpanSizeLookup(footerAdapter: MainLoadStateAdapter): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == adapter.itemCount && footerAdapter.itemCount > 0) {
                    2
                } else {
                    1
                }
            }
        }
    }

    private fun createScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (binding.fabFilter.isShown) {
                        binding.fabFilter.hide()
                    }
                } else if (dy < 0) {
                    if (binding.layoutAppBar.appBarLayout.isShown && !binding.fabFilter.isShown) {
                        binding.fabFilter.show()
                    }
                }
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                viewModel.clearFilters()

                adapter.refresh()
            }
            setColorSchemeResources(
                R.color.loading, R.color.loading
            )
            setProgressBackgroundColorSchemeResource(R.color.charade)
        }
    }

    private fun setupFilterButton() {
        binding.fabFilter.setOnClickListener {
            FilterEpisodeListFragment().show(
                requireActivity().supportFragmentManager,
                FilterEpisodeListFragment.TAG
            )
        }
    }

    private fun setupFilterFragmentResultListener() {
        setFragmentResultListener(SEARCH_EPISODE_LIST_RESULT_KEY) { _, bundle ->
            val name = bundle.getString(BUNDLE_NAME_KEY).orEmpty()
            val episode = bundle.getString(BUNDLE_EPISODE_KEY).orEmpty()

            val filterParams = EpisodeListViewModel.EpisodeFilterParams(
                name = name,
                episode = episode
            )
            viewModel.applyFilters(filterParams)
        }
    }

    private fun observeEpisodeList() {
        lifecycleScope.launch {
            viewModel.episodeListFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeLoadState() {
        adapter.loadStateFlow
            .debounce(500)
            .onEach { loadState ->
                mainLoadStateViewHolder.bind(loadState.refresh)
            }
            .launchIn(lifecycleScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleScrollingToTop() = lifecycleScope.launch {
        getRefreshLoadStateFlow(adapter)
            .simpleScan(count = 3)
            .collect { (previousState, currentState) ->
                if (previousState is LoadState.Loading
                    && currentState is LoadState.NotLoading
                    && _binding != null
                ) {
                    delay(200)
                    binding.recyclerView.scrollToPosition(0)
                }
            }
    }

    private fun getRefreshLoadStateFlow(adapter: EpisodeListAdapter): Flow<LoadState> {
        return adapter.loadStateFlow.map { it.refresh }
    }

    companion object {
        fun newInstance() = EpisodeListFragment()
    }
}