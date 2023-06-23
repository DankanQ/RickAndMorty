package com.dankanq.rickandmorty.di

import androidx.lifecycle.ViewModel
import com.dankanq.rickandmorty.utils.presentation.viewmodel.NetworkViewModel
import com.dankanq.rickandmorty.presentation.character.detail.CharacterViewModel
import com.dankanq.rickandmorty.presentation.character.main.CharacterListViewModel
import com.dankanq.rickandmorty.presentation.episode.detail.EpisodeViewModel
import com.dankanq.rickandmorty.presentation.episode.main.EpisodeListViewModel
import com.dankanq.rickandmorty.presentation.location.detail.LocationViewModel
import com.dankanq.rickandmorty.presentation.location.main.LocationListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(CharacterListViewModel::class)
    fun bindCharacterListViewModel(viewModel: CharacterListViewModel): ViewModel

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

    @Binds
    @IntoMap
    @ViewModelKey(LocationListViewModel::class)
    fun bindLocationListViewModel(viewModel: LocationListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LocationViewModel::class)
    fun bindLocationViewModel(viewModel: LocationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NetworkViewModel::class)
    fun bindNetworkViewModel(viewModel: NetworkViewModel): ViewModel
}