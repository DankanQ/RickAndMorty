package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.data.mapper.EpisodeMapper
import com.dankanq.rickandmorty.data.network.EpisodeApi
import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity
import com.dankanq.rickandmorty.utils.Constants.PAGE_SIZE
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
        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            val characterList = fetchCharacters()
            if (loadType == LoadType.REFRESH) {
                episodeDao.refresh(characterList)
            } else {
                episodeDao.save(characterList)
            }
            MediatorResult.Success(
                endOfPaginationReached = characterList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            if (episodeDao.getEpisodesCount() > 0) {
                return MediatorResult.Error(
                    LoadError.NetworkError(
                        true,
                        "Ошибка сети: ${e.message}"
                    )
                )
            } else {
                return MediatorResult.Error(
                    LoadError.NetworkError(
                        false,
                        "Ошибка сети: ${e.message}"
                    )
                )
            }
        } catch (e: HttpException) {
            return MediatorResult.Error(LoadError.HttpError(e.code()))
        } catch (e: Exception) {
            return MediatorResult.Error(LoadError.UnknownError)
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

    private suspend fun fetchCharacters(): List<EpisodeEntity> {
        return episodeApi.getEpisodeList(
            pageIndex,
            name = name,
            episode = episode
        )
            .episodeList
            .map { episodeMapper.mapCharacterDtoToEntity(it) }
    }

    sealed class LoadError : Exception() {
        data class NetworkError(
            val hasData: Boolean,
            override val message: String
        ) : LoadError()

        data class HttpError(val code: Int) : LoadError()
        object UnknownError : LoadError()
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