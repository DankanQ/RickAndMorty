package com.dankanq.rickandmorty.entity.character.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CharacterListDto(
    @SerializedName("results")
    @Expose
    val characterList: List<CharacterDto>
)