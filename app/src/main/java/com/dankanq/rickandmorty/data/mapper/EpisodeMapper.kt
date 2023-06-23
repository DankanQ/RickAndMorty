package com.dankanq.rickandmorty.data.mapper

import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity
import com.dankanq.rickandmorty.entity.episode.data.network.EpisodeDto
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.utils.data.Constants.NULL_STRING
import com.dankanq.rickandmorty.utils.data.formatDateString
import javax.inject.Inject

class EpisodeMapper @Inject constructor() {
    fun mapEpisodeDtoToEntity(episodeDto: EpisodeDto) = EpisodeEntity(
        id = episodeDto.id,
        name = episodeDto.name ?: NULL_STRING,
        airDate = episodeDto.airDate ?: NULL_STRING,
        episode = episodeDto.episode ?: NULL_STRING,
        characters = episodeDto.characters ?: mutableListOf(),
        url = episodeDto.airDate ?: NULL_STRING,
        created = episodeDto.created?.let { formatDateString(it) } ?: NULL_STRING
    )

    fun mapEpisodeEntityToModel(episodeEntity: EpisodeEntity) = Episode(
        id = episodeEntity.id,
        name = episodeEntity.name,
        airDate = episodeEntity.airDate,
        episode = episodeEntity.episode,
        characters = episodeEntity.characters,
        url = episodeEntity.url,
        created = episodeEntity.created
    )
}