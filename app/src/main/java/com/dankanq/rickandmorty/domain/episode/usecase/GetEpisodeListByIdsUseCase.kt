package com.dankanq.rickandmorty.domain.episode.usecase

import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import javax.inject.Inject

class GetEpisodeListByIdsUseCase @Inject constructor(
    private val repository: EpisodeRepository
) {
    suspend operator fun invoke(ids: String) = repository.getEpisodeListByIds(ids)
}