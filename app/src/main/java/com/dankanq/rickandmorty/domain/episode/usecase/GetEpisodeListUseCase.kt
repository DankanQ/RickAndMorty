package com.dankanq.rickandmorty.domain.episode.usecase

import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import javax.inject.Inject

class GetEpisodeListUseCase @Inject constructor(
    private val repository: EpisodeRepository
) {
    operator fun invoke(
        name: String?,
        episode: String?
    ) = repository.getEpisodeList(name, episode)
}