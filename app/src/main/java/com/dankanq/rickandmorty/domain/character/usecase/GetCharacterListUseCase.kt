package com.dankanq.rickandmorty.domain.character.usecase

import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import javax.inject.Inject

class GetCharacterListUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    operator fun invoke(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ) = repository.getCharacterList(name, status, species, type, gender)
}