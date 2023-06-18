package com.dankanq.rickandmorty.di

import androidx.lifecycle.ViewModel
import com.dankanq.rickandmorty.presentation.character.detail.CharacterViewModel
import com.dankanq.rickandmorty.presentation.character.main.CharactersViewModel
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
}