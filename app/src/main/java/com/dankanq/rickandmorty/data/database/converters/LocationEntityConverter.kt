package com.dankanq.rickandmorty.data.database.converters

import androidx.room.TypeConverter
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import com.google.gson.Gson

class LocationEntityConverter {
    @TypeConverter
    fun fromLocationEntity(locationEntity: CharacterEntity.LocationEntity): String {
        return Gson().toJson(locationEntity)
    }

    @TypeConverter
    fun toLocationEntity(json: String): CharacterEntity.LocationEntity {
        return Gson().fromJson(json, CharacterEntity.LocationEntity::class.java)
    }
}