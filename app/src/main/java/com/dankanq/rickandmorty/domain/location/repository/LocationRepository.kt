package com.dankanq.rickandmorty.domain.location.repository

import androidx.paging.PagingData
import com.dankanq.rickandmorty.entity.location.domain.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationList(
        name: String? = null,
        type: String? = null,
        dimension: String? = null
    ): Flow<PagingData<Location>>

    suspend fun getLocation(id: Long): Location

    fun loadLocation(id: Long): Flow<Unit>
}