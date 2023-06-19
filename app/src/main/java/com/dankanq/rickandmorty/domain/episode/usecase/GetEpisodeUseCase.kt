package com.dankanq.rickandmorty.domain.episode.usecase

import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import javax.inject.Inject

class GetEpisodeUseCase @Inject constructor(
    private val repository: EpisodeRepository
) {
    suspend operator fun invoke(id: Long) = repository.getEpisode(id)
}