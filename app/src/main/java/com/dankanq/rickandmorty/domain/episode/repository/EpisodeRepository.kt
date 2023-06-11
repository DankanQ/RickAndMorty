package com.dankanq.rickandmorty.domain.episode.repository

import com.dankanq.rickandmorty.entity.episode.domain.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    suspend fun getEpisodeList(ids: String): List<Episode>

    fun loadEpisodeList(ids:  String): Flow<Unit>
}