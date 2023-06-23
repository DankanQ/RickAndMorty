package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.data.mapper.EpisodeMapper
import com.dankanq.rickandmorty.data.network.EpisodeApi
import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity
import com.dankanq.rickandmorty.utils.data.Constants.PAGE_SIZE
import com.dankanq.rickandmorty.utils.presentation.model.LoadError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EpisodeRemoteMediator @AssistedInject constructor(
    private val episodeDao: EpisodeDao,
    private val episodeApi: EpisodeApi,
    private val episodeMapper: EpisodeMapper,
    @Assisted(KEY_NAME) private val name: String?,
    @Assisted(KEY_EPISODE) private val episode: String?
) : RemoteMediator<Int, EpisodeEntity>() {
    private var pageIndex = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EpisodeEntity>
    ): MediatorResult {
        var hasData = false

        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            hasData = episodeDao.hasData(name, episode)

            val episodeList = loadEpisodeList()
            if (loadType == LoadType.REFRESH) {
                episodeDao.refresh(
                    episodeList,
                    name, episode
                )
            } else {
                episodeDao.insertEpisodeList(episodeList)
            }

            MediatorResult.Success(
                endOfPaginationReached = episodeList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            val episodeCount = episodeDao.getEpisodeCount(name, episode)
            return MediatorResult.Error(
                LoadError.NetworkError(
                    e.message.toString(),
                    hasData,
                    episodeCount
                )
            )
        } catch (e: HttpException) {
            return MediatorResult.Error(LoadError.HttpError(e.code()))
        } catch (e: Exception) {
            return MediatorResult.Error(LoadError.UnknownError(hasData))
        }
    }

    private fun getPageIndex(loadType: LoadType): Int? {
        pageIndex = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return null
            LoadType.APPEND -> ++pageIndex
        }
        return pageIndex
    }

    private suspend fun loadEpisodeList(): List<EpisodeEntity> {
        return episodeApi.getEpisodeList(
            pageIndex,
            name = name,
            episode = episode
        )
            .episodeList
            .map { episodeMapper.mapEpisodeDtoToEntity(it) }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(KEY_NAME) name: String?,
            @Assisted(KEY_EPISODE) episode: String?
        ): EpisodeRemoteMediator
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_EPISODE = "episode"
    }
}