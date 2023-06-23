package com.dankanq.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.data.mapper.EpisodeMapper
import com.dankanq.rickandmorty.data.network.EpisodeApi
import com.dankanq.rickandmorty.data.paging.EpisodeRemoteMediator
import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import com.dankanq.rickandmorty.entity.episode.data.network.EpisodeDto
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.utils.data.Constants.PAGE_SIZE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EpisodeRepositoryImpl @Inject constructor(
    private val episodeApi: EpisodeApi,
    private val episodeDao: EpisodeDao,
    private val episodeRemoteMediatorFactory: EpisodeRemoteMediator.Factory,
    private val episodeMapper: EpisodeMapper
) : EpisodeRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getEpisodeList(
        name: String?,
        episode: String?
    ): Flow<PagingData<Episode>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            remoteMediator = episodeRemoteMediatorFactory.create(
                name = name,
                episode = episode
            ),
            pagingSourceFactory = {
                episodeDao.getPagingSource(
                    name,
                    episode
                )
            }
        )
            .flow
            .map { pagingData ->
                pagingData.map { episodeEntity ->
                    episodeMapper.mapEpisodeEntityToModel(episodeEntity)
                }
            }
    }

    override suspend fun getEpisodeListByIds(ids: String): List<Episode> {
        return withContext(Dispatchers.IO) {
            val idList = ids.split(",").map { it.trim().toLong() }
            val episodeList = episodeDao.getEpisodeList(idList).map { episodeEntity ->
                episodeMapper.mapEpisodeEntityToModel(episodeEntity)
            }
            episodeList
        }
    }

    override fun loadEpisodeListByIds(ids: String): Flow<Unit> = flow {
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

    override suspend fun getEpisode(id: Long): Episode {
        return withContext(Dispatchers.IO) {
            val episode = episodeMapper.mapEpisodeEntityToModel(
                episodeDao.getEpisode(id)
            )
            episode
        }
    }

    override fun loadEpisode(id: Long): Flow<Unit> = flow {
        val episodeDto = episodeApi.getEpisode(id)

        val episodeEntity =
            episodeMapper.mapEpisodeDtoToEntity(episodeDto)
        episodeDao.insertEpisode(episodeEntity)

        emit(Unit)
    }.flowOn(Dispatchers.IO)
}