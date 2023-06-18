package com.dankanq.rickandmorty.data.database.converters

import androidx.room.TypeConverter
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import com.google.gson.Gson

class OriginEntityConverter {
    @TypeConverter
    fun fromOriginEntity(originEntity: CharacterEntity.OriginEntity): String {
        return Gson().toJson(originEntity)
    }

    @TypeConverter
    fun toOriginEntity(json: String): CharacterEntity.OriginEntity {
        return Gson().fromJson(json, CharacterEntity.OriginEntity::class.java)
    }
}