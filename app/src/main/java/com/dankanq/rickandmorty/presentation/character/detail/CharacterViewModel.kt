package com.dankanq.rickandmorty.presentation.character.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.domain.character.usecase.GetCharacterUseCase
import com.dankanq.rickandmorty.domain.character.usecase.LoadCharacterUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.GetEpisodeListUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.LoadEpisodeListUseCase
import com.dankanq.rickandmorty.entity.character.domain.Character
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class CharacterViewModel @Inject constructor(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val loadCharacterUseCase: LoadCharacterUseCase,
    private val getEpisodeListUseCase: GetEpisodeListUseCase,
    private val loadEpisodeListUseCase: LoadEpisodeListUseCase
) : ViewModel() {
    private val characterId = MutableLiveData<Long>()
    private val episodeIds = MutableLiveData<String>()
    private var episodeIdsCount = 0

    private val shouldRetryLoadCharacter = MutableLiveData<Unit>()
    private val shouldRetryLoadEpisodeList = MutableLiveData<Unit>()

    private val loadingFlow = MutableSharedFlow<State>()

    private val _character = MutableLiveData<DatabaseResult<*>>()
    val character: LiveData<DatabaseResult<*>>
        get() = _character

    private val _episodeList = MutableLiveData<DatabaseResult<*>>()
    val episodeList: LiveData<DatabaseResult<*>>
        get() = _episodeList

    fun getCharacter() {
        viewModelScope.launch {
            val result = try {
                val successResult = getCharacterUseCase(characterId.value ?: -1)
                DatabaseResult.Success(successResult)
            } catch (e: Exception) {
                DatabaseResult.Error(e.message.toString())
            }
            _character.value = result
        }
    }

    fun getEpisodeList() {
        viewModelScope.launch {
            val result = try {
                val episodeList = getEpisodeListUseCase(episodeIds.value ?: "-1")
                if (episodeList.isNotEmpty()) {
                    DatabaseResult.Success(episodeList)
                } else {
                    DatabaseResult.Error("Episode list is empty")
                }
            } catch (e: Exception) {
                DatabaseResult.Error(e.message.toString())
            }
            _episodeList.value = result
        }
    }

    sealed class DatabaseResult<out T> {
        data class Success<out T>(val data: T) : DatabaseResult<T>()
        data class Error(val message: String) : DatabaseResult<Nothing>()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val loadCharacterDetailFlow: Flow<State> = shouldRetryLoadCharacter.asFlow()
        .flatMapLatest {
            loadCharacterUseCase(characterId.value!!)
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
    val episodesFlow: Flow<State> = shouldRetryLoadEpisodeList.asFlow()
        .flatMapLatest {
            loadEpisodeListUseCase(episodeIds.value!!)
                .map { State.Success(content = it) as State }
                .onStart { emit(State.Loading) }
                .mergeWith(loadingFlow)
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

    fun retryLoadCharacter() {
        shouldRetryLoadCharacter.value = Unit
    }

    fun retryLoadEpisodeList() {
        shouldRetryLoadEpisodeList.value = Unit
    }

    fun refresh() {
        shouldRetryLoadCharacter.value = Unit
        shouldRetryLoadEpisodeList.value = Unit
    }

    fun setupCharacterId(id: Long) {
        characterId.value = id
    }

    fun setupEpisodeIds(episodes: List<String>) {
        val ids = episodes.map { it.substringAfterLast("/") }
        val idsAsString = ids.joinToString(",")
        episodeIds.value = idsAsString
        episodeIdsCount = episodes.size
    }

    fun isEpisodeListDataComplete(episodeIdsCountFromDatabase: Int): Boolean {
        return episodeIdsCountFromDatabase >= episodeIdsCount
    }

    fun getParsedInfoMap(character: Character): Map<String, String> {
        val infoMap = mapOf(
            "Status" to character.status,
            "Species" to character.species,
            "Type" to character.type,
            "Gender" to character.gender,
            "Created" to character.created
        )
        val parsedInfoMap = mutableMapOf<String, String>()
        for (entry in infoMap.entries) {
            val value = entry.value.ifBlank { "~" }
            parsedInfoMap[entry.key] = value
        }
        return parsedInfoMap
    }

    fun getParsedLocationName(locationName: String): String {
        return locationName.ifBlank { "~" }
    }

    fun getColorIdByStatus(status: String): Int {
        return when (status) {
            "Alive" -> R.color.alive
            "Dead" -> R.color.dead
            else -> R.color.unknown
        }
    }

    private fun <T> Flow<T>.mergeWith(secondFlow: Flow<T>): Flow<T> {
        return merge(this, secondFlow)
    }

    sealed class State {
        object Loading : State()
        data class Success<T>(val content: T) : State()
        object Error : State()
    }
}