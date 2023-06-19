package com.dankanq.rickandmorty.di

import androidx.lifecycle.ViewModel
import com.dankanq.rickandmorty.presentation.character.detail.CharacterViewModel
import com.dankanq.rickandmorty.presentation.character.main.CharactersViewModel
import com.dankanq.rickandmorty.presentation.episode.detail.EpisodeViewModel
import com.dankanq.rickandmorty.presentation.episode.main.EpisodeListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(CharactersViewModel::class)
    fun bindCharactersViewModel(viewModel: CharactersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CharacterViewModel::class)
    fun bindCharacterViewModel(viewModel: CharacterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EpisodeListViewModel::class)
    fun bindEpisodeListViewModel(viewModel: EpisodeListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EpisodeViewModel::class)
    fun bindEpisodeViewModel(viewModel: EpisodeViewModel): ViewModel
}