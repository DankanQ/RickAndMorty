package com.dankanq.rickandmorty.domain.location.usecase

import com.dankanq.rickandmorty.domain.location.repository.LocationRepository
import javax.inject.Inject

class GetLocationListUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(
        name: String?,
        type: String?,
        dimension: String?
    ) = repository.getLocationList(name, type, dimension)
}