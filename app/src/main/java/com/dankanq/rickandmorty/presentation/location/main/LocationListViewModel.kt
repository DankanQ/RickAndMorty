package com.dankanq.rickandmorty.presentation.location.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dankanq.rickandmorty.domain.location.usecase.GetLocationListUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class LocationListViewModel @Inject constructor(
    private val getLocationListUseCase: GetLocationListUseCase
) : ViewModel() {
    private val filterParams = MutableLiveData(LocationFilterParams())

    val locationListFlow = filterParams.asFlow()
        .distinctUntilChanged()
        .flatMapLatest { params ->
            getLocationListUseCase(
                name = params.name,
                type = params.type,
                dimension = params.dimension
            )
        }
        .cachedIn(viewModelScope)

    fun applyFilters(params: LocationFilterParams) {
        filterParams.value = params
    }

    data class LocationFilterParams(
        val name: String? = null,
        val type: String? = null,
        val dimension: String? = null
    )
}