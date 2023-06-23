package com.dankanq.rickandmorty.presentation.episode.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.dankanq.rickandmorty.domain.character.usecase.GetCharacterListByIdsUseCase
import com.dankanq.rickandmorty.domain.character.usecase.LoadCharacterListByIdsUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.GetEpisodeUseCase
import com.dankanq.rickandmorty.domain.episode.usecase.LoadEpisodeUseCase
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

class EpisodeViewModel @Inject constructor(
    private val getEpisodeUseCase: GetEpisodeUseCase,
    private val loadEpisodeUseCase: LoadEpisodeUseCase,
    private val getCharacterListByIdsUseCase: GetCharacterListByIdsUseCase,
    private val loadCharacterListByIdsUseCase: LoadCharacterListByIdsUseCase
) : ViewModel() {
    private val episodeId = MutableLiveData<Long>()
    private val characterIds = MutableLiveData<String>()
    private var characterIdsCount = 0

    private val shouldRetryLoadEpisode = MutableLiveData<Unit>()
    private val shouldRetryLoadCharacterList = MutableLiveData<Unit>()

    private val _episode = MutableLiveData<DatabaseResult<*>>()
    val episode: LiveData<DatabaseResult<*>>
        get() = _episode

    private val _characterList = MutableLiveData<DatabaseResult<*>>()
    val characterList: LiveData<DatabaseResult<*>>
        get() = _characterList

    fun getEpisode() {
        viewModelScope.launch {
            val result = try {
                val successResult = getEpisodeUseCase(episodeId.value ?: -1)
                DatabaseResult.Success(successResult)
            } catch (e: Exception) {
                DatabaseResult.Error(e.message.toString())
            }
            _episode.value = result
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val loadEpisodeFlow: Flow<State> = shouldRetryLoadEpisode.asFlow()
        .flatMapLatest {
            loadEpisodeUseCase(episodeId.value!!)
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

    fun retryLoadEpisode() {
        shouldRetryLoadEpisode.value = Unit
    }

    fun retryLoadCharacterList() {
        shouldRetryLoadCharacterList.value = Unit
    }

    fun refresh() {
        shouldRetryLoadEpisode.value = Unit
        shouldRetryLoadCharacterList.value = Unit
    }

    fun setupEpisodeId(id: Long) {
        episodeId.value = id
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
}