package com.dankanq.rickandmorty.data.database.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        return data.split(",")
    }
}