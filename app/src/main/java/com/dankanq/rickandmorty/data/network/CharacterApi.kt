package com.dankanq.rickandmorty.data.network

import com.dankanq.rickandmorty.entity.character.data.network.CharacterDto
import com.dankanq.rickandmorty.entity.character.data.network.CharacterListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterApi {
    @GET("character")
    suspend fun getCharacterList(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("species") species: String? = null,
        @Query("type") type: String? = null,
        @Query("gender") gender: String? = null,
    ): CharacterListDto

    @GET("character/{id}")
    suspend fun getCharacterById(
        @Path("id") id: Long
    ): CharacterDto
}