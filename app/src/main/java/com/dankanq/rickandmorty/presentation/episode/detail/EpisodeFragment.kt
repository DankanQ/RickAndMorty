package com.dankanq.rickandmorty.presentation.episode.detail

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
import com.dankanq.rickandmorty.databinding.FragmentEpisodeBinding
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
import com.dankanq.rickandmorty.presentation.episode.detail.adapters.CharacterAdapter
import com.dankanq.rickandmorty.utils.presentation.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import javax.inject.Inject

class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding: FragmentEpisodeBinding
        get() = _binding ?: throw RuntimeException("FragmentEpisodeBinding is null")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[EpisodeViewModel::class.java]
    }

    private val component by lazy {
        (requireActivity().application as RickAndMortyApp).component
    }

    private var episodeId: Long = UNDEFINED_EPISODE_ID

    private val characterAdapter by lazy {
        CharacterAdapter(requireContext())
    }

    private var hasEpisodeLocaleData: Boolean = false

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
        _binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBarLayout()
        setupSwipeRefreshLayout()
        setupRetryEpisodeButton()
        setupRetryCharacterListButton()

        observeEpisode()
        observeCharacterList()

        viewModel.run {
            setupEpisodeId(episodeId)
            getEpisode()
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

    private fun setupRetryEpisodeButton() {
        binding.mainLoadState.bRetry.setOnClickListener {
            viewModel.retryLoadEpisode()
        }
    }

    private fun setupRetryCharacterListButton() {
        binding.episodesLoadState.bRetrySecond.setOnClickListener {
            viewModel.retryLoadCharacterList()
        }
    }

    private fun observeEpisode() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.episode.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is EpisodeViewModel.DatabaseResult.Success -> {
                            hasEpisodeLocaleData = true

                            val episode = result.data as Episode

                            with(binding) {
                                tvName.text = episode.name
                                tvEpisode.text = episode.episode
                                tvAirDate.text = episode.airDate

                                viewModel.run {
                                    setupCharacterIds(episode.characters)
                                    getCharacterList()
                                }

                                llContent.isVisible = true
                            }
                        }
                        is EpisodeViewModel.DatabaseResult.Error -> {
                            hasEpisodeLocaleData = false

                            viewModel.retryLoadEpisode()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.loadEpisodeFlow.collect { state ->
                    when (state) {
                        is EpisodeViewModel.State.Loading -> {
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
                        is EpisodeViewModel.State.Success<*> -> {
                            with(binding) {
                                mainLoadState.apply {
                                    progressBar.isVisible = false
                                    llMainLoadingState.isVisible = false
                                }
                                swipeRefreshLayout.isEnabled = true

                                viewModel.getEpisode()
                            }
                        }
                        is EpisodeViewModel.State.Error -> {
                            if (hasEpisodeLocaleData) {
                                Toast.makeText(
                                    requireContext(),
                                    "Network error. Unable to refresh the page.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.getEpisode()

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
                        is EpisodeViewModel.DatabaseResult.Success -> {
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

                            binding.episodesLoadState.progressBar.isVisible = false
                            val isEpisodeListDataComplete =
                                viewModel.isCharacterListDataComplete(characterList.size)
                            binding.episodesLoadState.llSecondLoadingState.isVisible =
                                !isEpisodeListDataComplete
                        }
                        is EpisodeViewModel.DatabaseResult.Error -> {
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
                        is EpisodeViewModel.State.Loading -> {
                            with(binding) {
                                episodesLoadState.apply {
                                    llSecondLoadingState.isVisible = false
                                    progressBar.isVisible = true
                                }
                            }
                        }
                        is EpisodeViewModel.State.Success<*> -> {
                            viewModel.getCharacterList()
                        }
                        is EpisodeViewModel.State.Error -> {
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

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(ID_KEY)) {
            throw RuntimeException("Args screen mode is absent")
        }
        episodeId = args.getLong(ID_KEY)
    }

    companion object {
        private const val ID_KEY = "id"
        private const val UNDEFINED_EPISODE_ID = 0L

        fun newInstance(id: Long): EpisodeFragment {
            return EpisodeFragment().apply {
                arguments = Bundle().apply {
                    putLong(ID_KEY, id)
                }
            }
        }
    }
}