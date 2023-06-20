package com.dankanq.rickandmorty.domain.location.usecase

import com.dankanq.rickandmorty.domain.location.repository.LocationRepository
import javax.inject.Inject

class LoadLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(id: Long) = repository.loadLocation(id)
}