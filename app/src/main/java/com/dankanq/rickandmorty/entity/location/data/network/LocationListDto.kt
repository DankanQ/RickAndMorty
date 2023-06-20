package com.dankanq.rickandmorty.entity.location.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationListDto(
    @SerializedName("results")
    @Expose
    val locationList: List<LocationDto>
)