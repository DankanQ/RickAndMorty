package com.dankanq.rickandmorty.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dankanq.rickandmorty.entity.episode.data.database.EpisodeEntity

@Dao
interface EpisodeDao {
    @Query(
        "SELECT * FROM episodes " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:episode IS NULL OR episode LIKE '%' || :episode  || '%')"
    )
    fun getPagingSource(
        name: String?,
        episode: String?
    ): PagingSource<Int, EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(episodeList: List<EpisodeEntity>)

    @Query("DELETE FROM episodes")
    suspend fun clear()

    @Transaction
    suspend fun refresh(episodeList: List<EpisodeEntity>) {
        clear()
        save(episodeList)
    }

    @Query("SELECT COUNT(*) FROM episodes")
    suspend fun getEpisodesCount(): Int


    @Query("SELECT * FROM episodes WHERE id == :id LIMIT 1")
    fun getEpisode(id: Long): EpisodeEntity

    @Query("SELECT * FROM episodes WHERE id IN (:idList)")
    fun getEpisodeList(idList: List<Long>): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeList(episodeList: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)
}