package com.dankanq.rickandmorty.domain.location.usecase

import com.dankanq.rickandmorty.domain.location.repository.LocationRepository
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(id: Long) = repository.getLocation(id)
}