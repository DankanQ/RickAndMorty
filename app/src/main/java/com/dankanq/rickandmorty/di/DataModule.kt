package com.dankanq.rickandmorty.di

import android.app.Application
import com.dankanq.rickandmorty.utils.Constants.BASE_URL
import com.dankanq.rickandmorty.data.database.AppDatabase
import com.dankanq.rickandmorty.data.database.dao.CharacterDao
import com.dankanq.rickandmorty.data.database.dao.EpisodeDao
import com.dankanq.rickandmorty.data.network.CharacterApi
import com.dankanq.rickandmorty.data.network.EpisodeApi
import com.dankanq.rickandmorty.data.repository.CharacterRepositoryImpl
import com.dankanq.rickandmorty.data.repository.EpisodeRepositoryImpl
import com.dankanq.rickandmorty.domain.character.repository.CharacterRepository
import com.dankanq.rickandmorty.domain.episode.repository.EpisodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
interface DataModule {
    @AppScope
    @Binds
    fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @AppScope
    @Binds
    fun bindEpisodeRepository(impl: EpisodeRepositoryImpl): EpisodeRepository

    companion object {
        @AppScope
        @Provides
        fun provideRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @AppScope
        @Provides
        fun provideCharacterApi(retrofit: Retrofit): CharacterApi {
            return retrofit.create(CharacterApi::class.java)
        }

        @AppScope
        @Provides
        fun provideCharacterDao(application: Application): CharacterDao {
            return AppDatabase.getInstance(application).characterDao()
        }

        @AppScope
        @Provides
        fun provideEpisodeApi(retrofit: Retrofit): EpisodeApi {
            return retrofit.create(EpisodeApi::class.java)
        }

        @AppScope
        @Provides
        fun provideEpisodeDao(application: Application): EpisodeDao {
            return AppDatabase.getInstance(application).episodeDao()
        }
    }
}