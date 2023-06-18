package com.dankanq.rickandmorty.domain.character.repository

import androidx.paging.PagingData
import com.dankanq.rickandmorty.entity.character.domain.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacterList(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        type: String? = null,
        gender: String? = null
    ): Flow<PagingData<Character>>

    suspend fun getCharacter(id: Long): Character

    fun loadCharacter(id: Long): Flow<Unit>
}