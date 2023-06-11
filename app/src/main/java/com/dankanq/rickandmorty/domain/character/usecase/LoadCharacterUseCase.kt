package com.dankanq.rickandmorty.domain.character.usecase

import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import javax.inject.Inject

class LoadCharacterUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    operator fun invoke(id: Long) = repository.loadCharacter(id)
}