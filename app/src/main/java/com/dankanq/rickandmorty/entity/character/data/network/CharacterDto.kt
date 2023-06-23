package com.dankanq.rickandmorty.entity.character.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CharacterDto(
    @SerializedName("id")
    @Expose
    val id: Long,
    @SerializedName("name")
    @Expose
    val name: String?,
    @SerializedName("status")
    @Expose
    val status: String?,
    @SerializedName("species")
    @Expose
    val species: String?,
    @SerializedName("type")
    @Expose
    val type: String?,
    @SerializedName("gender")
    @Expose
    val gender: String?,
    @SerializedName("origin")
    @Expose
    val originDto: OriginDto?,
    @SerializedName("location")
    @Expose
    val locationDto: LocationDto?,
    @SerializedName("image")
    @Expose
    val image: String?,
    @SerializedName("episode")
    @Expose
    val episode: List<String>?,
    @SerializedName("url")
    @Expose
    val url: String?,
    @SerializedName("created")
    @Expose
    val created: String?
) {
    data class OriginDto(
        @SerializedName("name")
        @Expose
        val name: String?,
        @SerializedName("url")
        @Expose
        val url: String?
    )

    data class LocationDto(
        @SerializedName("name")
        @Expose
        val name: String?,
        @SerializedName("url")
        @Expose
        val url: String?
    )
}