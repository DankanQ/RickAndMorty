package com.dankanq.rickandmorty.data.repository

import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.data.mapper.EpisodeMapper
import com.dankanq.rickandmorty.data.network.EpisodeApi
import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import com.dankanq.rickandmorty.entity.episode.data.network.EpisodeDto
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EpisodeRepositoryImpl @Inject constructor(
    private val episodeApi: EpisodeApi,
    private val episodeDao: EpisodeDao,
    private val episodeMapper: EpisodeMapper
) : EpisodeRepository {
    override suspend fun getEpisodeList(ids: String): List<Episode> {
        return withContext(Dispatchers.IO) {
            val idList = ids.split(",").map { it.trim().toLong() }
            val episodeList = episodeDao.getEpisodeList(idList).map { episodeEntity ->
                episodeMapper.mapEpisodeEntityToModel(episodeEntity)
            }
            episodeList
        }
    }

    override fun loadEpisodeList(ids: String): Flow<Unit> = flow {
        val response = episodeApi.getEpisodesByIds(ids)
        val episodeList: List<EpisodeDto> = if (response.isJsonObject) {
            val episodeDto = Gson().fromJson(response, EpisodeDto::class.java)
            listOf(episodeDto)
        } else {
            val episodesDto = Gson().fromJson<List<EpisodeDto>>(
                response,
                object : TypeToken<List<EpisodeDto>>() {}.type
            )
            episodesDto
        }

        val episodeEntityList = episodeList.map { episodeDto ->
            episodeMapper.mapEpisodeDtoToEntity(episodeDto)
        }
        episodeDao.insertEpisodeList(episodeEntityList)

        emit(Unit)
    }.flowOn(Dispatchers.IO)
}