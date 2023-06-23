package com.dankanq.rickandmorty.utils.presentation.model

sealed class DatabaseResult<out T> {
    data class Success<out T>(val data: T) : DatabaseResult<T>()
    data class Error(val message: String) : DatabaseResult<Nothing>()
}