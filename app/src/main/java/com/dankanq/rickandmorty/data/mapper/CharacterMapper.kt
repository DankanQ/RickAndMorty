package com.dankanq.rickandmorty.data.mapper

import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import com.dankanq.rickandmorty.entity.character.data.network.CharacterDto
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.utils.Constants.NULL_STRING
import com.dankanq.rickandmorty.utils.data.formatDateString
import javax.inject.Inject

class CharacterMapper @Inject constructor() {
    fun mapCharacterDtoToEntity(characterDto: CharacterDto) = CharacterEntity(
        id = characterDto.id,
        name = characterDto.name ?: NULL_STRING,
        status = characterDto.status ?: NULL_STRING,
        species = characterDto.species ?: NULL_STRING,
        type = characterDto.type ?: NULL_STRING,
        gender = characterDto.gender ?: NULL_STRING,
        originEntity = characterDto.originDto?.let { mapOriginDtoToEntity(it) }
            ?: CharacterEntity.OriginEntity(
                NULL_STRING,
                NULL_STRING
            ),
        locationEntity = characterDto.locationDto?.let { mapLocationDtoToEntity(it) }
            ?: CharacterEntity.LocationEntity(
                NULL_STRING,
                NULL_STRING
            ),
        image = characterDto.image ?: NULL_STRING,
        episode = characterDto.episode ?: mutableListOf(),
        url = characterDto.url ?: NULL_STRING,
        created = characterDto.created?.let { formatDateString(it) } ?: NULL_STRING
    )

    fun mapCharacterEntityToModel(characterEntity: CharacterEntity) = Character(
        id = characterEntity.id,
        name = characterEntity.name,
        status = characterEntity.status,
        species = characterEntity.species,
        type = characterEntity.type,
        gender = characterEntity.gender,
        origin = mapOriginEntityToModel(characterEntity.originEntity),
        location = mapLocationEntityToModel(characterEntity.locationEntity),
        image = characterEntity.image,
        episode = characterEntity.episode,
        url = characterEntity.url,
        created = characterEntity.created
    )

    private fun mapOriginDtoToEntity(originDto: CharacterDto.OriginDto) =
        CharacterEntity.OriginEntity(
            name = originDto.name ?: NULL_STRING,
            url = originDto.url ?: NULL_STRING
        )

    private fun mapOriginEntityToModel(originEntity: CharacterEntity.OriginEntity) =
        Character.Origin(
            name = originEntity.name,
            url = originEntity.url
        )

    private fun mapLocationDtoToEntity(locationDto: CharacterDto.LocationDto) =
        CharacterEntity.LocationEntity(
            name = locationDto.name ?: NULL_STRING,
            url = locationDto.url ?: NULL_STRING
        )

    private fun mapLocationEntityToModel(locationEntity: CharacterEntity.LocationEntity) =
        Character.Location(
            name = locationEntity.name,
            url = locationEntity.url
        )
}