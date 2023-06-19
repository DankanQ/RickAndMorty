package com.dankanq.rickandmorty.data.network

import com.dankanq.rickandmorty.entity.episode.data.network.EpisodeDto
import com.dankanq.rickandmorty.entity.episode.data.network.EpisodeListDto
import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EpisodeApi {
    @GET("episode")
    suspend fun getEpisodeList(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("episode") episode: String? = null
    ): EpisodeListDto

    @GET("character/{id}")
    suspend fun getEpisode(
        @Path("id") id: Long
    ): EpisodeDto

    @GET("episode/{ids}")
    suspend fun getEpisodesByIds(
        @Path("ids") ids: String
    ): JsonElement
}