package com.dankanq.rickandmorty.entity.character.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val originEntity: OriginEntity,
    val locationEntity: LocationEntity,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
) {
    data class OriginEntity(
        val name: String,
        val url: String
    )

    data class LocationEntity(
        val name: String,
        val url: String
    )
}