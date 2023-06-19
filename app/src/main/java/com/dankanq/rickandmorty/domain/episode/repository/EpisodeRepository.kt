package com.dankanq.rickandmorty.domain.episode.repository

import androidx.paging.PagingData
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    fun getEpisodeList(
        name: String? = null,
        episode: String? = null
    ): Flow<PagingData<Episode>>

    suspend fun getEpisodeListByIds(ids: String): List<Episode>

    fun loadEpisodeListByIds(ids: String): Flow<Unit>

    suspend fun getEpisode(id: Long): Episode

    fun loadEpisode(id: Long): Flow<Unit>
}