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
import com.dankanq.rickandmorty.presentation.NetworkViewModel
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
import com.dankanq.rickandmorty.utils.presentation.ViewModelFactory
import com.dankanq.rickandmorty.utils.presentation.adapter.CharacterAdapter
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

        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            .isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAppBarLayout() {
        binding.bBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                viewModel.refresh()
            }
            setColorSchemeResources(
                R.color.white, R.color.white
            )
            setProgressBackgroundColorSchemeResource(R.color.ship_gray)
        }
    }

    private fun setupRetryLocationButton() {
        binding.mainLoadState.bRetry.setOnClickListener {
            viewModel.retryLoadLocation()
        }
    }

    private fun setupRetryCharacterListButton() {
        binding.charactersLoadState.bRetrySecond.setOnClickListener {
            viewModel.retryLoadCharacterList()
        }
    }

    private fun observeLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.location.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is LocationViewModel.DatabaseResult.Success -> {
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

                                llContent.isVisible = true
                            }
                        }
                        is LocationViewModel.DatabaseResult.Error -> {
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
                        is LocationViewModel.State.Loading -> {
                            with(binding) {
                                mainLoadState.apply {
                                    progressBar.isVisible = true
                                    llMainLoadingState.isVisible = false
                                }
                                swipeRefreshLayout.isEnabled = false
                                if (mainLoadState.progressBar.isVisible) {
                                    swipeRefreshLayout.isRefreshing = false
                                }
                                llContent.isVisible = false
                            }
                        }
                        is LocationViewModel.State.Success<*> -> {
                            with(binding) {
                                mainLoadState.apply {
                                    progressBar.isVisible = false
                                    llMainLoadingState.isVisible = false
                                }
                                swipeRefreshLayout.isEnabled = true

                                viewModel.getLocation()
                            }
                        }
                        is LocationViewModel.State.Error -> {
                            if (hasLocationLocaleData) {
                                Toast.makeText(
                                    requireContext(),
                                    "Network error. Unable to refresh the page.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.getLocation()

                                binding.swipeRefreshLayout.isEnabled = true
                            } else {
                                with(binding) {
                                    mainLoadState.llMainLoadingState.isVisible = true
                                    llContent.isVisible = false
                                    swipeRefreshLayout.isEnabled = false
                                }
                            }
                            binding.mainLoadState.progressBar.isVisible = false
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
                        is LocationViewModel.DatabaseResult.Success -> {
                            val characterList = result.data as List<Character>

                            binding.rvCharacters.adapter = characterAdapter
                            characterAdapter.onCharacterClick = { character ->
                                val fragment = CharacterFragment.newInstance(character.id)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container_view, fragment)
                                    .addToBackStack("null")
                                    .commit()
                            }
                            characterAdapter.submitList(characterList)

                            binding.charactersLoadState.progressBar.isVisible = false
                            val isLocationListDataComplete =
                                viewModel.isCharacterListDataComplete(characterList.size)
                            binding.charactersLoadState.llSecondLoadingState.isVisible =
                                !isLocationListDataComplete
                        }
                        is LocationViewModel.DatabaseResult.Error -> {
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
                        is LocationViewModel.State.Loading -> {
                            with(binding) {
                                charactersLoadState.apply {
                                    llSecondLoadingState.isVisible = false
                                    progressBar.isVisible = true
                                }
                            }
                        }
                        is LocationViewModel.State.Success<*> -> {
                            viewModel.getCharacterList(isConnected)
                        }
                        is LocationViewModel.State.Error -> {
                            with(binding) {
                                charactersLoadState.apply {
                                    progressBar.isVisible = false
                                    llSecondLoadingState.isVisible = true
                                }
                            }
                        }
                    }
                }
            }
        }
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