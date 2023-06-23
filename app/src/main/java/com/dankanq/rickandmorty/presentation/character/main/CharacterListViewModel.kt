package com.dankanq.rickandmorty.presentation.character.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dankanq.rickandmorty.domain.character.usecase.GetCharacterListUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModel @Inject constructor(
    private val getCharacterListUseCase: GetCharacterListUseCase
) : ViewModel() {
    private val characterFilterParams = MutableLiveData(CharacterFilterParams())

    val charactersFlow = characterFilterParams.asFlow()
        .distinctUntilChanged()
        .flatMapLatest { params ->
            getCharacterListUseCase(
                name = params.name,
                status = params.status,
                species = params.species,
                type = params.type,
                gender = params.gender
            )
        }
        .cachedIn(viewModelScope)

    fun applyFilters(params: CharacterFilterParams) {
        characterFilterParams.value = params
    }

    fun clearFilters() {
        characterFilterParams.value = CharacterFilterParams()
    }

    data class CharacterFilterParams(
        val name: String? = null,
        val status: String? = null,
        val species: String? = null,
        val type: String? = null,
        val gender: String? = null
    )
}