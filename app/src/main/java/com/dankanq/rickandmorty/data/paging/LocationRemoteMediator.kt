package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.LocationDao
import com.dankanq.rickandmorty.data.mapper.LocationMapper
import com.dankanq.rickandmorty.data.network.LocationApi
import com.dankanq.rickandmorty.entity.location.data.database.LocationEntity
import com.dankanq.rickandmorty.utils.Constants.PAGE_SIZE
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class LocationRemoteMediator @AssistedInject constructor(
    private val locationDao: LocationDao,
    private val locationApi: LocationApi,
    private val locationMapper: LocationMapper,
    @Assisted(KEY_NAME) private val name: String?,
    @Assisted(KEY_TYPE) private val type: String?,
    @Assisted(KEY_DIMENSION) private val dimension: String?
) : RemoteMediator<Int, LocationEntity>() {
    private var pageIndex = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LocationEntity>
    ): MediatorResult {
        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            val locationList = fetchLocationList()
            if (loadType == LoadType.REFRESH) {
                locationDao.refresh(locationList)
            } else {
                locationDao.save(locationList)
            }
            MediatorResult.Success(
                endOfPaginationReached = locationList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            if (locationDao.getLocationsCount() > 0) {
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

    private suspend fun fetchLocationList(): List<LocationEntity> {
        return locationApi.getLocationList(
            pageIndex,
            name = name,
            type = type,
            dimension = dimension
        )
            .locationList
            .map { locationMapper.mapLocationDtoToEntity(it) }
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
            @Assisted(KEY_TYPE) type: String?,
            @Assisted(KEY_DIMENSION) dimension: String?
        ): LocationRemoteMediator
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_TYPE = "type"
        private const val KEY_DIMENSION = "dimension"
    }
}