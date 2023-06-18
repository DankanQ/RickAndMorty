package com.dankanq.rickandmorty.utils.domain

// TODO: использовать вместо State
sealed class ResultWrapper<out T> {
    object Loading: ResultWrapper<Nothing>()

    data class Success<out T>(val data: T): ResultWrapper<T>()

    object NetworkError : ResultWrapper<Nothing>()
    data class GenericError(val message: String?) : ResultWrapper<Nothing>()
}