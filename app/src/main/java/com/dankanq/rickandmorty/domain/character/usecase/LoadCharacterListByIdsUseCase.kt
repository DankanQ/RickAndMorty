package com.dankanq.rickandmorty.domain.character.usecase

import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import javax.inject.Inject

class LoadCharacterListByIdsUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    operator fun invoke(ids: String) = repository.loadCharacterListByIds(ids)
}