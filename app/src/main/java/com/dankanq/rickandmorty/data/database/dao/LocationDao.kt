package com.dankanq.rickandmorty.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dankanq.rickandmorty.entity.location.data.database.LocationEntity

@Dao
interface LocationDao {
    @Query(
        "SELECT * FROM locations " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:dimension IS NULL OR dimension LIKE '%' || :dimension  || '%')"
    )
    fun getPagingSource(
        name: String?,
        type: String?,
        dimension: String?,
    ): PagingSource<Int, LocationEntity>

    @Query(
        "DELETE FROM locations " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:dimension IS NULL OR dimension LIKE '%' || :dimension  || '%')"
    )
    suspend fun clear(
        name: String?,
        type: String?,
        dimension: String?
    )

    @Transaction
    suspend fun refresh(
        locationList: List<LocationEntity>,
        name: String?,
        type: String?,
        dimension: String?,
    ) {
        clear(name, type, dimension)
        insertLocationList(locationList)
    }

    @Query(
        "SELECT COUNT(*) FROM locations " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:dimension IS NULL OR dimension LIKE '%' || :dimension  || '%')"
    )
    suspend fun getLocationCount(name: String?, type: String?, dimension: String?): Int

    @Transaction
    suspend fun hasData(name: String?, type: String?, dimension: String?): Boolean {
        return getLocationCount(name, type, dimension) > 0
    }

    @Query("SELECT * FROM locations WHERE id == :id LIMIT 1")
    fun getLocation(id: Long): LocationEntity

    @Query("SELECT * FROM locations WHERE id IN (:idList)")
    fun getLocationList(idList: List<Long>): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationList(locationList: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)
}