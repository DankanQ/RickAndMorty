package com.dankanq.rickandmorty.data.mapper

import com.dankanq.rickandmorty.entity.location.data.database.LocationEntity
import com.dankanq.rickandmorty.entity.location.data.network.LocationDto
import com.dankanq.rickandmorty.entity.location.domain.Location
import com.dankanq.rickandmorty.utils.data.Constants.NULL_STRING
import com.dankanq.rickandmorty.utils.data.formatDateString
import javax.inject.Inject

class LocationMapper @Inject constructor() {
    fun mapLocationDtoToEntity(locationDto: LocationDto) = LocationEntity(
        id = locationDto.id,
        name = locationDto.name ?: NULL_STRING,
        type = locationDto.type ?: NULL_STRING,
        dimension = locationDto.dimension ?: NULL_STRING,
        residents = locationDto.residents ?: mutableListOf(),
        url = locationDto.url ?: NULL_STRING,
        created = locationDto.created?.let { formatDateString(it) } ?: NULL_STRING
    )

    fun mapLocationEntityToModel(locationDto: LocationEntity) = Location(
        id = locationDto.id,
        name = locationDto.name,
        type = locationDto.type,
        dimension = locationDto.dimension,
        residents = locationDto.residents,
        url = locationDto.url,
        created = locationDto.created
    )
}