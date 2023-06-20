package com.dankanq.rickandmorty.entity.location.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("id")
    @Expose
    val id: Long,
    @SerializedName("name")
    @Expose
    val name: String?,
    @SerializedName("type")
    @Expose
    val type: String?,
    @SerializedName("dimension")
    @Expose
    val dimension: String?,
    @SerializedName("residents")
    @Expose
    val residents: List<String>?,
    @SerializedName("url")
    @Expose
    val url: String?,
    @SerializedName("created")
    @Expose
    val created: String?
)
