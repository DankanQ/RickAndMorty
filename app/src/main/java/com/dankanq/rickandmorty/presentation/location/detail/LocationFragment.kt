package com.dankanq.rickandmorty.presentation.location.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.RickAndMortyApp
import com.dankanq.rickandmorty.databinding.FragmentLocationBinding
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.entity.location.domain.Location
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
import com.dankanq.rickandmorty.presentation.episode.detail.adapter.CharacterAdapter
import com.dankanq.rickandmorty.utils.domain.State
import com.dankanq.rickandmorty.utils.presentation.model.DatabaseResult
import com.dankanq.rickandmorty.utils.presentation.viewmodel.NetworkViewModel
import com.dankanq.rickandmorty.utils.presentation.viewmodel.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding: FragmentLocationBinding
        get() = _binding ?: throw RuntimeException("FragmentLocationBinding is null")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[LocationViewModel::class.java]
    }

    private val networkViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
    }

    private val component by lazy {
        (requireActivity().application as RickAndMortyApp).component
    }

    private val characterAdapter by lazy {
        CharacterAdapter(requireContext())
    }

    private var locationId: Long = UNDEFINED_LOCATION_ID
    private var hasLocationLocaleData: Boolean = false
    private var isConnected: Boolean = false

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBarLayout()
        setupSwipeRefreshLayout()
        setupRetryLocationButton()
        setupRetryCharacterListButton()

        observeLocation()
        observeCharacterList()

        networkViewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            this.isConnected = isConnected
        }

        viewModel.run {
            setupLocationId(locationId)
            getLocation()
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAppBarLayout() {
        binding.layoutAppBarDetail.apply {
            tvLabel.text = getText(R.string.location_detail)
            bBack.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                viewModel.refresh()
            }
            setColorSchemeResources(
                R.color.loading, R.color.loading
            )
            setProgressBackgroundColorSchemeResource(R.color.charade)
        }
    }

    private fun setupRetryLocationButton() {
        binding.mainLoadState.bRetry.setOnClickListener {
            viewModel.retryLoadLocation()
        }
    }

    private fun setupRetryCharacterListButton() {
        binding.secondLoadState.bRetrySecond.setOnClickListener {
            viewModel.retryLoadCharacterList()
        }
    }

    private fun observeLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.location.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is DatabaseResult.Success -> {
                            hasLocationLocaleData = true

                            val location = result.data as Location

                            with(binding) {
                                tvName.text = location.name
                                tvType.text = location.type
                                tvDimension.text = location.dimension

                                viewModel.run {
                                    setupCharacterIds(location.residents)
                                    getCharacterList(isConnected)
                                }

                                swipeRefreshLayout.isEnabled = true
                                llContent.isVisible = true
                            }
                        }
                        is DatabaseResult.Error -> {
                            hasLocationLocaleData = false

                            viewModel.retryLoadLocation()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.loadLocationFlow.collect { state ->
                    when (state) {
                        is State.Loading -> {
                            with(binding) {
                                mainLoadState.llMainLoadingState.isVisible = false
                                swipeRefreshLayout.apply {
                                    isEnabled = false
                                    isRefreshing = true
                                }
                            }
                        }
                        is State.Success<*> -> {
                            with(binding) {
                                mainLoadState.llMainLoadingState.isVisible = false
                                swipeRefreshLayout.apply {
                                    isEnabled = true
                                    isRefreshing = false
                                }

                                viewModel.getLocation()
                            }
                        }
                        is State.Error -> {
                            if (hasLocationLocaleData) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_cant_refresh_detail),
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.getLocation()

                            } else {
                                binding.mainLoadState.apply {
                                    tvLoadError.text = getString(R.string.error_network)
                                    tvLoadErrorInfo.isVisible = false
                                    llMainLoadingState.isVisible = true
                                }
                            }
                            binding.swipeRefreshLayout.apply {
                                isRefreshing = false
                                isEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeCharacterList() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.characterList.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is DatabaseResult.Success -> {
                            val characterList = result.data as List<Character>

                            binding.rvCharacters.adapter = characterAdapter
                            characterAdapter.onCharacterClick = { character ->
                                launchCharacterFragment(character.id)
                            }
                            characterAdapter.submitList(characterList)

                            val isLocationListDataComplete =
                                viewModel.isCharacterListDataComplete(characterList.size)
                            binding.secondLoadState.apply {
                                llSecondLoadState.isVisible =
                                    !isLocationListDataComplete
                                progressBar.isVisible = false
                            }
                        }
                        is DatabaseResult.Error -> {
                            viewModel.retryLoadCharacterList()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.characterListFlow.collect { state ->
                    when (state) {
                        is State.Loading -> {
                            with(binding) {
                                secondLoadState.apply {
                                    llSecondLoadState.isVisible = false
                                    progressBar.isVisible = true
                                }
                            }
                        }
                        is State.Success<*> -> {
                            viewModel.getCharacterList(isConnected)
                        }
                        is State.Error -> {
                            with(binding) {
                                secondLoadState.apply {
                                    progressBar.isVisible = false
                                    llSecondLoadState.isVisible = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchCharacterFragment(characterId: Long) {
        val fragment = CharacterFragment.newInstance(characterId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(ID_KEY)) {
            throw RuntimeException("Args screen mode is absent")
        }
        locationId = args.getLong(ID_KEY)
    }

    companion object {
        private const val ID_KEY = "id"
        private const val UNDEFINED_LOCATION_ID = 0L

        fun newInstance(id: Long): LocationFragment {
            return LocationFragment().apply {
                arguments = Bundle().apply {
                    putLong(ID_KEY, id)
                }
            }
        }
    }
}