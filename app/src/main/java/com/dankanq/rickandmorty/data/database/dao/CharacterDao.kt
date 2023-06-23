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

    @Query(
        "DELETE FROM characters " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:status IS NULL OR status LIKE '%' || :status  || '%') " +
                "AND (:species IS NULL OR species LIKE '%' || :species  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:gender IS NULL OR gender LIKE '%' || :gender  || '%')"
    )
    suspend fun clear(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    )

    @Transaction
    suspend fun refresh(
        characters: List<CharacterEntity>,
        name: String?,
        species: String?,
        type: String?,
        gender: String?,
        status: String?,
    ) {
        clear(name, status, species, type, gender)
        insertCharacterList(characters)
    }

    @Query(
        "SELECT COUNT(*) FROM characters " +
                "WHERE (:name IS NULL OR name LIKE '%' || :name  || '%') " +
                "AND (:status IS NULL OR status LIKE '%' || :status  || '%') " +
                "AND (:species IS NULL OR species LIKE '%' || :species  || '%') " +
                "AND (:type IS NULL OR type LIKE '%' || :type  || '%') " +
                "AND (:gender IS NULL OR gender LIKE '%' || :gender  || '%')"
    )
    suspend fun getCharacterCount(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ): Int

    @Transaction
    suspend fun hasData(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ): Boolean {
        return getCharacterCount(name, status, species, type, gender) > 0
    }

    @Query("SELECT * FROM characters WHERE id == :id LIMIT 1")
    fun getCharacter(id: Long): CharacterEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE id IN (:idList)")
    fun getCharacterList(idList: List<Long>): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterList(characterList: List<CharacterEntity>)
}