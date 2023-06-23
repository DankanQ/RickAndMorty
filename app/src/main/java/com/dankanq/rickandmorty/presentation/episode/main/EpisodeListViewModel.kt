package com.dankanq.rickandmorty.presentation.episode.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dankanq.rickandmorty.domain.episode.usecase.GetEpisodeListUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeListViewModel @Inject constructor(
    private val getEpisodeListUseCase: GetEpisodeListUseCase
) : ViewModel() {
    private val episodeFilterParams = MutableLiveData(EpisodeFilterParams())

    val episodeListFlow = episodeFilterParams.asFlow()
        .distinctUntilChanged()
        .flatMapLatest { params ->
            getEpisodeListUseCase(
                name = params.name,
                episode = params.episode
            )
        }
        .cachedIn(viewModelScope)

    fun applyFilters(params: EpisodeFilterParams) {
        episodeFilterParams.value = params
    }

    fun clearFilters() {
        episodeFilterParams.value = EpisodeFilterParams()
    }

    data class EpisodeFilterParams(
        val name: String? = null,
        val episode: String? = null
    )
}