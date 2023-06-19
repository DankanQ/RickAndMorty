package com.dankanq.rickandmorty.domain.episode.usecase

import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import javax.inject.Inject

class LoadEpisodeListByIdsUseCase @Inject constructor(
    private val repository: EpisodeRepository
) {
    operator fun invoke(ids: String) = repository.loadEpisodeListByIds(ids)
}