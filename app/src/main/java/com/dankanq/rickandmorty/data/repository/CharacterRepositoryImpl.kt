package com.dankanq.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dankanq.rickandmorty.data.database.dao.CharacterDao
import com.dankanq.rickandmorty.data.mapper.CharacterMapper
import com.dankanq.rickandmorty.data.network.CharacterApi
import com.dankanq.rickandmorty.data.paging.CharacterRemoteMediator
import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import com.dankanq.rickandmorty.entity.character.data.network.CharacterDto
import com.dankanq.rickandmorty.entity.character.domain.Character
import com.dankanq.rickandmorty.utils.Constants.PAGE_SIZE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterApi: CharacterApi,
    private val characterRemoteMediatorFactory: CharacterRemoteMediator.Factory,
    private val characterMapper: CharacterMapper
) : CharacterRepository {
//    private val refreshEvents = MutableSharedFlow<Unit>()

    override fun getCharacterList(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            remoteMediator = characterRemoteMediatorFactory.create(
                name = name,
                status = status,
                species = species,
                type = type,
                gender = gender
            ),
            pagingSourceFactory = {
                characterDao.getPagingSource(
                    name,
                    status,
                    species,
                    type,
                    gender
                )
            }
        )
            .flow
            .map { pagingData ->
                pagingData.map { characterEntity ->
                    characterMapper.mapCharacterEntityToModel(characterEntity)
                }
            }
    }

    override suspend fun getCharacter(id: Long): Character {
        return withContext(Dispatchers.IO) {
            val character = characterMapper.mapCharacterEntityToModel(
                characterDao.getCharacter(id)
            )
            character
        }
    }

    override fun loadCharacter(id: Long): Flow<Unit> = flow {
        val characterDto = characterApi.getCharacterById(id)

        val characterEntity =
            characterMapper.mapCharacterDtoToEntity(characterDto)
        characterDao.insertCharacter(characterEntity)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    override suspend fun getCharacterListByIds(ids: String): List<Character> {
        return withContext(Dispatchers.IO) {
            val idList = ids.split(",").map { it.trim().toLong() }
            val characterList = characterDao.getCharacterList(idList).map { characterEntity ->
                characterMapper.mapCharacterEntityToModel(characterEntity)
            }
            characterList
        }
    }

    override fun loadCharacterListByIds(ids: String): Flow<Unit> = flow {
        val response = characterApi.getCharacterListByIds(ids)
        val characterList: List<CharacterDto> = if (response.isJsonObject) {
            val characterDto = Gson().fromJson(response, CharacterDto::class.java)
            listOf(characterDto)
        } else {
            val charactersDto = Gson().fromJson<List<CharacterDto>>(
                response,
                object : TypeToken<List<CharacterDto>>() {}.type
            )
            charactersDto
        }

        val characterEntityList = characterList.map { characterDto ->
            characterMapper.mapCharacterDtoToEntity(characterDto)
        }
        characterDao.insertCharacterList(characterEntityList)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

//    override suspend fun refresh() {
//        refreshEvents.emit(Unit)
//    }
}