package com.dankanq.rickandmorty.entity.episode.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EpisodeListDto(
    @SerializedName("results")
    @Expose
    val episodeList: List<EpisodeDto>
)