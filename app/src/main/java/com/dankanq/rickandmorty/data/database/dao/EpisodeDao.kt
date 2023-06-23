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

    @Query(
        "DELETE FROM episodes " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:episode IS NULL OR episode LIKE '%' || :episode  || '%')"
    )
    suspend fun clear(
        name: String?,
        episode: String?
    )

    @Transaction
    suspend fun refresh(
        episodeList: List<EpisodeEntity>,
        name: String?,
        episode: String?
    ) {
        clear(name, episode)
        insertEpisodeList(episodeList)
    }

    @Query(
        "SELECT COUNT(*) FROM episodes " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:episode IS NULL OR episode LIKE '%' || :episode  || '%')"
    )
    suspend fun getEpisodeCount(name: String?, episode: String?): Int

    @Transaction
    suspend fun hasData(name: String?, episode: String?): Boolean {
        return getEpisodeCount(name, episode) > 0
    }

    @Query("SELECT * FROM episodes WHERE id == :id LIMIT 1")
    fun getEpisode(id: Long): EpisodeEntity

    @Query("SELECT * FROM episodes WHERE id IN (:idList)")
    fun getEpisodeList(idList: List<Long>): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeList(episodeList: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes")
    suspend fun clear()
}