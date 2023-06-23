package com.dankanq.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dankanq.rickandmorty.data.database.dao.LocationDao
import com.dankanq.rickandmorty.data.mapper.LocationMapper
import com.dankanq.rickandmorty.data.network.LocationApi
import com.dankanq.rickandmorty.data.paging.LocationRemoteMediator
import com.dankanq.rickandmorty.domain.location.repository.LocationRepository
import com.dankanq.rickandmorty.entity.location.domain.Location
import com.dankanq.rickandmorty.utils.data.Constants.PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
    private val locationDao: LocationDao,
    private val locationRemoteMediatorFactory: LocationRemoteMediator.Factory,
    private val locationMapper: LocationMapper
) : LocationRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getLocationList(
        name: String?,
        type: String?,
        dimension: String?
    ): Flow<PagingData<Location>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            remoteMediator = locationRemoteMediatorFactory.create(
                name = name,
                type = type,
                dimension = dimension
            ),
            pagingSourceFactory = {
                locationDao.getPagingSource(
                    name,
                    type,
                    dimension
                )
            }
        )
            .flow
            .map { pagingData ->
                pagingData.map { locationEntity ->
                    locationMapper.mapLocationEntityToModel(locationEntity)
                }
            }
    }

    override suspend fun getLocation(id: Long): Location {
        return withContext(Dispatchers.IO) {
            val location = locationMapper.mapLocationEntityToModel(
                locationDao.getLocation(id)
            )
            location
        }
    }

    override fun loadLocation(id: Long): Flow<Unit> = flow {
        val locationDto = locationApi.getLocation(id)

        val locationEntity =
            locationMapper.mapLocationDtoToEntity(locationDto)
        locationDao.insertLocation(locationEntity)

        emit(Unit)
    }.flowOn(Dispatchers.IO)
}