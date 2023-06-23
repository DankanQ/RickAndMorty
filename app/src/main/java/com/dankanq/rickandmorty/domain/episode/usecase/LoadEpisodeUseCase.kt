package com.dankanq.rickandmorty.domain.episode.usecase

import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import javax.inject.Inject

class LoadEpisodeUseCase @Inject constructor(
    private val repository: EpisodeRepository
) {
    operator fun invoke(id: Long) = repository.loadEpisode(id)
}