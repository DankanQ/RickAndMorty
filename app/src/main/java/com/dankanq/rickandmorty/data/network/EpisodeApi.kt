package com.dankanq.rickandmorty.data.network

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path

interface EpisodeApi {
    @GET("episode/{ids}")
    suspend fun getEpisodesByIds(
        @Path("ids") ids: String
    ): JsonElement
}