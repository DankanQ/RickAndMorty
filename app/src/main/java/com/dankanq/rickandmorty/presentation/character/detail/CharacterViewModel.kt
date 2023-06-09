package com.dankanq.rickandmorty.presentation.character.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.domain.character.usecase.GetCharacterUseCase
import com.dankanq.rickandmorty.domain.character.usecase.LoadCharacterUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.GetEpisodeListByIdsUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.LoadEpisodeListByIdsUseCase
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.utils.domain.State
import com.dankanq.rickandmorty.utils.presentation.model.DatabaseResult
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

class CharacterViewModel @Inject constructor(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val loadCharacterUseCase: LoadCharacterUseCase,
    private val getEpisodeListByIdsUseCase: GetEpisodeListByIdsUseCase,
    private val loadEpisodeListByIdsUseCase: LoadEpisodeListByIdsUseCase
) : ViewModel() {
    private val characterId = MutableLiveData<Long>()
    private val episodeIds = MutableLiveData<String>()
    private var episodeIdsCount = 0

    private val shouldRetryLoadCharacter = MutableLiveData<Unit>()
    private val shouldRetryLoadEpisodeList = MutableLiveData<Unit>()

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

    fun getEpisodeList(isConnected: Boolean) {
        viewModelScope.launch {
            val result = try {
                val episodeList = getEpisodeListByIdsUseCase(episodeIds.value ?: "-1")
                if (episodeList.isNotEmpty()) {
                    if (episodeList.size < episodeIdsCount && isConnected) {
                        shouldRetryLoadEpisodeList.value = Unit
                    }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val loadCharacterFlow: Flow<State> = shouldRetryLoadCharacter.asFlow()
        .flatMapLatest {
            loadCharacterUseCase(characterId.value!!)
                .map { State.Success(content = it) as State }
                .onStart { emit(State.Loading) }
                .catch { emit(State.Error) }
        }
        .shareIn(
            viewModelScope,
            SharingStarted.Lazily,
            1
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val episodeListFlow: Flow<State> = shouldRetryLoadEpisodeList.asFlow()
        .flatMapLatest {
            loadEpisodeListByIdsUseCase(episodeIds.value!!)
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
}