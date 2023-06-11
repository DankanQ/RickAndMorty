package com.dankanq.rickandmorty.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dankanq.rickandmorty.data.database.converters.LocationEntityConverter
import com.dankanq.rickandmorty.data.database.converters.OriginEntityConverter
import com.dankanq.rickandmorty.data.database.converters.StringListConverter
import com.dankanq.rickandmorty.data.database.dao.CharacterDao
import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity

@Database(
    entities = [
        CharacterEntity::class,
        EpisodeEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    OriginEntityConverter::class,
    LocationEntityConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun episodeDao(): EpisodeDao

    companion object {
        private var database: AppDatabase? = null
        private const val DB_NAME = "RickAndMorty.db"
        private val LOCK = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(LOCK) {
                database?.let { return it }
                val instance =
                    Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        DB_NAME
                    ).fallbackToDestructiveMigration()
                        .build()
                database = instance
                return instance
            }
        }
    }
}