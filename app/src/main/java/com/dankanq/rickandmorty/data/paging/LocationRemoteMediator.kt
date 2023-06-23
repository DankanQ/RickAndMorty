package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.LocationDao
import com.dankanq.rickandmorty.data.mapper.LocationMapper
import com.dankanq.rickandmorty.data.network.LocationApi
import com.dankanq.rickandmorty.entity.location.data.database.LocationEntity
import com.dankanq.rickandmorty.utils.data.Constants.PAGE_SIZE
import com.dankanq.rickandmorty.utils.presentation.model.LoadError
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
        var hasData = false

        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            hasData = locationDao.hasData(name, type, dimension)

            val locationList = loadLocationList()
            if (loadType == LoadType.REFRESH) {
                locationDao.refresh(
                    locationList,
                    name,
                    type,
                    dimension
                )
            } else {
                locationDao.insertLocationList(locationList)
            }

            MediatorResult.Success(
                endOfPaginationReached = locationList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            val locationCount = locationDao.getLocationCount(name, type, dimension)
            return MediatorResult.Error(
                LoadError.NetworkError(
                    e.message.toString(),
                    hasData,
                    locationCount
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

    private suspend fun loadLocationList(): List<LocationEntity> {
        return locationApi.getLocationList(
            pageIndex,
            name = name,
            type = type,
            dimension = dimension
        )
            .locationList
            .map { locationMapper.mapLocationDtoToEntity(it) }
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