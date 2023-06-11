package com.dankanq.rickandmorty.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE id IN (:idList)")
    fun getEpisodeList(idList: List<Long>): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeList(episodeList: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)
}