package com.dankanq.rickandmorty.utils.domain

sealed class State {
    object Loading : State()
    data class Success<T>(val content: T) : State()
    object Error : State()
}