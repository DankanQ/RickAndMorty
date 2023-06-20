package com.dankanq.rickandmorty.presentation.location.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.dankanq.rickandmorty.domain.character.usecase.GetCharacterListByIdsUseCase
import com.dankanq.rickandmorty.domain.character.usecase.LoadCharacterListByIdsUseCase
import com.dankanq.rickandmorty.domain.location.usecase.GetLocationUseCase
import com.dankanq.rickandmorty.domain.location.usecase.LoadLocationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationViewModel @Inject constructor(
    private val getLocationUseCase: GetLocationUseCase,
    private val loadLocationUseCase: LoadLocationUseCase,
    private val getCharacterListByIdsUseCase: GetCharacterListByIdsUseCase,
    private val loadCharacterListByIdsUseCase: LoadCharacterListByIdsUseCase
) : ViewModel() {
    private val locationId = MutableLiveData<Long>()
    private val characterIds = MutableLiveData<String>()
    private var characterIdsCount = 0

    private val shouldRetryLoadLocation = MutableLiveData<Unit>()
    private val shouldRetryLoadCharacterList = MutableLiveData<Unit>()

    private val _location = MutableLiveData<DatabaseResult<*>>()
    val location: LiveData<DatabaseResult<*>>
        get() = _location

    private val _characterList = MutableLiveData<DatabaseResult<*>>()
    val characterList: LiveData<DatabaseResult<*>>
        get() = _characterList

    fun getLocation() {
        viewModelScope.launch {
            val result = try {
                val successResult = getLocationUseCase(locationId.value ?: -1)
                DatabaseResult.Success(successResult)
            } catch (e: Exception) {
                DatabaseResult.Error(e.message.toString())
            }
            _location.value = result
        }
    }

    fun getCharacterList(isConnected: Boolean) {
        viewModelScope.launch {
            val result = try {
                val characterList = getCharacterListByIdsUseCase(characterIds.value ?: "-1")
                if (characterList.isNotEmpty()) {
                    if (characterList.size < characterIdsCount && isConnected) {
                        shouldRetryLoadCharacterList.value = Unit
                    }
                    DatabaseResult.Success(characterList)
                } else {
                    DatabaseResult.Error("Character list is empty")
                }
            } catch (e: Exception) {
                DatabaseResult.Error(e.message.toString())
            }
            _characterList.value = result
        }
    }

    sealed class DatabaseResult<out T> {
        data class Success<out T>(val data: T) : DatabaseResult<T>()
        data class Error(val message: String) : DatabaseResult<Nothing>()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val loadLocationFlow: Flow<State> = shouldRetryLoadLocation.asFlow()
        .flatMapLatest {
            loadLocationUseCase(locationId.value!!)
                .map { State.Success(content = it) as State }
                .onStart { emit(State.Loading) }
                .retry(2) {
                    delay(1000)
                    true
                }
                .catch { emit(State.Error) }
        }
        .shareIn(
            viewModelScope,
            SharingStarted.Lazily,
            1
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val characterListFlow: Flow<State> = shouldRetryLoadCharacterList.asFlow()
        .flatMapLatest {
            loadCharacterListByIdsUseCase(characterIds.value!!)
                .map { State.Success(content = it) as State }
                .onStart { emit(State.Loading) }
                .retry(2) {
                    delay(1000)
                    true
                }
                .catch { emit(State.Error) }
        }
        .shareIn(
            viewModelScope,
            SharingStarted.Lazily,
            1
        )

    fun retryLoadLocation() {
        shouldRetryLoadLocation.value = Unit
    }

    fun retryLoadCharacterList() {
        shouldRetryLoadCharacterList.value = Unit
    }

    fun refresh() {
        shouldRetryLoadLocation.value = Unit
        shouldRetryLoadCharacterList.value = Unit
    }

    fun setupLocationId(id: Long) {
        locationId.value = id
    }

    fun setupCharacterIds(characters: List<String>) {
        val ids = characters.map { it.substringAfterLast("/") }
        val idsAsString = ids.joinToString(",")
        characterIds.value = idsAsString
        characterIdsCount = characters.size
    }

    fun isCharacterListDataComplete(characterIdsCountFromDatabase: Int): Boolean {
        return characterIdsCountFromDatabase >= characterIdsCount
    }

    sealed class State {
        object Loading : State()
        data class Success<T>(val content: T) : State()
        object Error : State()
    }
}