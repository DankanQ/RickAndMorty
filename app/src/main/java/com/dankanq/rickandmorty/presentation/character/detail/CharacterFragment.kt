package com.dankanq.rickandmorty.presentation.character.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.RickAndMortyApp
import com.dankanq.rickandmorty.databinding.FragmentCharacterBinding
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.presentation.NetworkViewModel
import com.dankanq.rickandmorty.presentation.character.detail.adapters.EpisodeAdapter
import com.dankanq.rickandmorty.presentation.character.detail.adapters.InfoSection
import com.dankanq.rickandmorty.presentation.episode.detail.EpisodeFragment
import com.dankanq.rickandmorty.utils.presentation.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

class CharacterFragment : Fragment() {
    private var _binding: FragmentCharacterBinding? = null
    private val binding: FragmentCharacterBinding
        get() = _binding ?: throw RuntimeException("FragmentCharacterBinding is null")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[CharacterViewModel::class.java]
    }

    private val networkViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
    }

    private val component by lazy {
        (requireActivity().application as RickAndMortyApp).component
    }

    private val episodesAdapter by lazy {
        EpisodeAdapter()
    }

    private var characterId: Long = UNDEFINED_CHARACTER_ID
    private var hasCharacterLocaleData: Boolean = false
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
        _binding = FragmentCharacterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBarLayout()
        setupSwipeRefreshLayout()
        setupRetryDetailButton()
        setupRetryEpisodeListButton()

        observeCharacterDetail()
        observeEpisodes()

        networkViewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            this.isConnected = isConnected
        }

        viewModel.run {
            setupCharacterId(characterId)
            getCharacter()
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

    private fun setupRetryDetailButton() {
        binding.mainLoadState.bRetry.setOnClickListener {
            viewModel.retryLoadCharacter()
        }
    }

    private fun setupRetryEpisodeListButton() {
        binding.episodesLoadState.bRetrySecond.setOnClickListener {
            viewModel.retryLoadEpisodeList()
        }
    }

    private fun observeCharacterDetail() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.character.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is CharacterViewModel.DatabaseResult.Success -> {
                            hasCharacterLocaleData = true

                            val character = result.data as Character

                            with(binding) {
                                Glide.with(requireContext())
                                    .load(character.image)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(ivImage)

                                tvName.text = character.name

                                setupRecyclerViewInfo(character)

                                setupOrigin(character.origin)

                                tvLocation.apply {
                                    text = viewModel.getParsedLocationName(character.location.name)
                                    setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            viewModel.getColorIdByStatus(character.status)
                                        )
                                    )
                                }

                                viewModel.run {
                                    setupEpisodeIds(character.episode)
                                    getEpisodeList(isConnected)
                                }

                                llContent.isVisible = true
                            }
                        }
                        is CharacterViewModel.DatabaseResult.Error -> {
                            hasCharacterLocaleData = false

                            viewModel.retryLoadCharacter()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.loadCharacterDetailFlow.collect { state ->
                    when (state) {
                        is CharacterViewModel.State.Loading -> {
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
                        is CharacterViewModel.State.Success<*> -> {
                            with(binding) {
                                mainLoadState.apply {
                                    progressBar.isVisible = false
                                    llMainLoadingState.isVisible = false
                                }
                                swipeRefreshLayout.isEnabled = true

                                viewModel.getCharacter()
                            }
                        }
                        is CharacterViewModel.State.Error -> {
                            if (hasCharacterLocaleData) {
                                Toast.makeText(
                                    requireContext(),
                                    "Network error. Unable to refresh the page.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.getCharacter()

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

    private fun observeEpisodes() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.episodeList.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is CharacterViewModel.DatabaseResult.Success -> {
                            val episodeList = result.data as List<Episode>

                            binding.rvEpisodes.adapter = episodesAdapter
                            episodesAdapter.onEpisodeClick = { episode ->
                                val fragment = EpisodeFragment.newInstance(episode.id)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container_view, fragment)
                                    .addToBackStack("null")
                                    .commit()
                            }
                            episodesAdapter.submitList(episodeList)

                            binding.episodesLoadState.progressBar.isVisible = false
                            val isEpisodeListDataComplete =
                                viewModel.isEpisodeListDataComplete(episodeList.size)
                            binding.episodesLoadState.llSecondLoadingState.isVisible =
                                !isEpisodeListDataComplete
                        }
                        is CharacterViewModel.DatabaseResult.Error -> {
                            viewModel.retryLoadEpisodeList()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.episodesFlow.collect { state ->
                    when (state) {
                        is CharacterViewModel.State.Loading -> {
                            with(binding) {
                                episodesLoadState.apply {
                                    llSecondLoadingState.isVisible = false
                                    progressBar.isVisible = true
                                }
                            }
                        }
                        is CharacterViewModel.State.Success<*> -> {
                            viewModel.getEpisodeList(isConnected)
                        }
                        is CharacterViewModel.State.Error -> {
                            with(binding) {
                                episodesLoadState.apply {
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

    private fun setupRecyclerViewInfo(character: Character) {
        val infoMap = viewModel.getParsedInfoMap(character)
        val sectionedAdapter = SectionedRecyclerViewAdapter()
        for (info in infoMap) {
            sectionedAdapter.addSection(
                InfoSection(info.key, info.value, requireContext())
            )
        }
        binding.rvInfo.adapter = sectionedAdapter
    }

    private fun setupOrigin(origin: Character.Origin) {
        when (origin.name) {
            getString(R.string.unknown) -> {
                binding.cvOrigin.isVisible = false
            }
            else -> {
                with(binding) {
                    tvOrigin.text = origin.name
                    cvOrigin.isVisible = true
                }
            }
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(ID_KEY)) {
            throw RuntimeException("Args screen mode is absent")
        }
        characterId = args.getLong(ID_KEY)
    }

    companion object {
        private const val ID_KEY = "id"
        private const val UNDEFINED_CHARACTER_ID = 0L

        fun newInstance(id: Long): CharacterFragment {
            return CharacterFragment().apply {
                arguments = Bundle().apply {
                    putLong(ID_KEY, id)
                }
            }
        }
    }
}