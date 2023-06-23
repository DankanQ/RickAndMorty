package com.dankanq.rickandmorty.utils.presentation.model

sealed class LoadError : Exception() {
    data class NetworkError(
        override val message: String,
        val hasData: Boolean,
        val characterCount: Int
    ) : LoadError()

    data class HttpError(val code: Int) : LoadError()
    data class UnknownError(val hasData: Boolean) : LoadError()
}