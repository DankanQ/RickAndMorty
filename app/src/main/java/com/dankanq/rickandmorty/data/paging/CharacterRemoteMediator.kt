package com.dankanq.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dankanq.rickandmorty.data.database.dao.CharacterDao
import com.dankanq.rickandmorty.data.mapper.CharacterMapper
import com.dankanq.rickandmorty.data.network.CharacterApi
import com.dankanq.rickandmorty.entity.character.data.database.CharacterEntity
import com.dankanq.rickandmorty.utils.data.Constants.PAGE_SIZE
import com.dankanq.rickandmorty.utils.presentation.model.LoadError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

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
        var hasData = false

        return try {
            pageIndex = getPageIndex(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            hasData = characterDao.hasData(name, status, species, type, gender)

            val characterList = loadCharacterList()
            if (loadType == LoadType.REFRESH) {
                characterDao.refresh(
                    characterList,
                    name, status, species, type, gender
                )
            } else {
                characterDao.insertCharacterList(characterList)
            }

            MediatorResult.Success(
                endOfPaginationReached = characterList.size < PAGE_SIZE
            )
        } catch (e: IOException) {
            val characterCount = characterDao.getCharacterCount(name, status, species, type, gender)
            return MediatorResult.Error(
                LoadError.NetworkError(
                    e.message.toString(),
                    hasData,
                    characterCount
                )
            )
        } catch (e: HttpException) {
            return MediatorResult.Error(LoadError.HttpError(e.code()))
        } catch (e: Exception) {
            return MediatorResult.Error(LoadError.UnknownError(hasData))
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

    private suspend fun loadCharacterList(): List<CharacterEntity> {
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
        private const val KEY_NAME = "name"
        private const val KEY_STATUS = "status"
        private const val KEY_SPECIES = "species"
        private const val KEY_TYPE = "type"
        private const val KEY_GENDER = "gender"
    }
}