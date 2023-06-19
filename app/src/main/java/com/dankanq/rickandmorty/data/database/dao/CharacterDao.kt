package com.dankanq.rickandmorty.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity

@Dao
interface CharacterDao {
    @Query(
        "SELECT * FROM characters " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:status IS NULL OR status LIKE '%' || :status  || '%') " +
                "AND (:species IS NULL OR species LIKE '%' || :species  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:gender IS NULL OR gender LIKE '%' || :gender  || '%')"
    )
    fun getPagingSource(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ): PagingSource<Int, CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(characterList: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun clear()

    @Transaction
    suspend fun refreshWithInternet(characterList: List<CharacterEntity>) {
        clear()
        save(characterList)
    }

    @Transaction
    suspend fun refresh(characterList: List<CharacterEntity>) {
        clear()
        save(characterList)
    }

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharacterCount(): Int

    @Query("SELECT * FROM characters WHERE id == :id LIMIT 1")
    fun getCharacter(id: Long): CharacterEntity


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE id IN (:idList)")
    fun getCharacterList(idList: List<Long>): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterList(characterList: List<CharacterEntity>)
}