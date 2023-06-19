package com.dankanq.rickandmorty.domain.character.usecase

import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import javax.inject.Inject

class GetCharacterListByIdsUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(ids: String) = repository.getCharacterListByIds(ids)
}