package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.CharacterDao
import com.dankanq.rickandmorty.data.mapper.CharacterMapper
import com.dankanq.rickandmorty.data.network.CharacterApi
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

// TODO: переделать RemoteMediator для большей гибкости
@OptIn(ExperimentalPagingApi::class)
class CharacterRemoteMediator @AssistedInject constructor(
    private val characterDao: CharacterDao,
    private val characterApi: CharacterApi,
    private val characterMapper: CharacterMapper,
    @Assisted(KEY_NAME) private val name: String?,
    @Assisted(KEY_STATUS) private val status: String?,
    @Assisted(KEY_SPECIES) private val species: String?,
    @Assisted(KEY_TYPE) private val type: String?,
    @Assisted(KEY_GENDER) private val gender: String?
) : RemoteMediator<Int, CharacterEntity>() {
    private var pageIndex = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {
        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            val characterList = fetchCharacters()
            if (loadType == LoadType.REFRESH) {
                characterDao.refresh(characterList)
            } else {
                characterDao.save(characterList)
            }
            MediatorResult.Success(
                endOfPaginationReached = characterList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            if (characterDao.getCharacterCount() > 0) {
                return MediatorResult.Error(
                    LoadError.NetworkError(
                        true,
                        "Ошибка сети: ${e.message}"
                    )
                )
            } else {
                return MediatorResult.Error(
                    LoadError.NetworkError(
                        false,
                        "Ошибка сети: ${e.message}"
                    )
                )
            }
        } catch (e: HttpException) {
            return MediatorResult.Error(LoadError.HttpError(e.code()))
        } catch (e: Exception) {
            return MediatorResult.Error(LoadError.UnknownError)
        }
    }

    private fun getPageIndex(loadType: LoadType): Int? {
        pageIndex = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return null
            LoadType.APPEND -> ++pageIndex
        }
        return pageIndex
    }

    private suspend fun fetchCharacters(): List<CharacterEntity> {
        return characterApi.getCharacterList(
            pageIndex,
            name = name,
            status = status,
            species = species,
            type = type,
            gender = gender
        )
            .characterList
            .map { characterMapper.mapCharacterDtoToEntity(it) }
    }

    sealed class LoadError : Exception() {
        data class NetworkError(
            val hasData: Boolean,
            override val message: String
        ) : LoadError()

        data class HttpError(val code: Int) : LoadError()
        object UnknownError : LoadError()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(KEY_NAME) name: String?,
            @Assisted(KEY_STATUS) status: String?,
            @Assisted(KEY_SPECIES) species: String?,
            @Assisted(KEY_TYPE) type: String?,
            @Assisted(KEY_GENDER) gender: String?
        ): CharacterRemoteMediator
    }

    companion object {
        const val PAGE_SIZE = 20

        private const val KEY_NAME = "name"
        private const val KEY_STATUS = "status"
        private const val KEY_SPECIES = "species"
        private const val KEY_TYPE = "type"
        private const val KEY_GENDER = "gender"
    }
}