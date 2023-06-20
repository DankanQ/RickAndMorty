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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(locationList: List<LocationEntity>)

    @Query("DELETE FROM locations")
    suspend fun clear()

    @Transaction
    suspend fun refresh(locationList: List<LocationEntity>) {
        clear()
        save(locationList)
    }

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationsCount(): Int


    @Query("SELECT * FROM locations WHERE id == :id LIMIT 1")
    fun getLocation(id: Long): LocationEntity

    @Query("SELECT * FROM locations WHERE id IN (:idList)")
    fun getLocationList(idList: List<Long>): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationList(locationList: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)
}