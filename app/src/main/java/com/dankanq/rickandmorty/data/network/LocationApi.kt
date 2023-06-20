package com.dankanq.rickandmorty.data.network

import com.dankanq.rickandmorty.entity.location.data.network.LocationDto
import com.dankanq.rickandmorty.entity.location.data.network.LocationListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocationApi {
    @GET("location")
    suspend fun getLocationList(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("type") type: String? = null,
        @Query("dimension") dimension: String? = null
    ): LocationListDto

    @GET("location/{id}")
    suspend fun getLocation(
        @Path("id") id: Long
    ): LocationDto
}