package com.dankanq.rickandmorty.di

import android.app.Application
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
import com.dankanq.rickandmorty.presentation.character.main.CharacterListFragment
import com.dankanq.rickandmorty.presentation.episode.detail.EpisodeFragment
import com.dankanq.rickandmorty.presentation.episode.main.EpisodeListFragment
import com.dankanq.rickandmorty.presentation.location.detail.LocationFragment
import com.dankanq.rickandmorty.presentation.location.main.LocationListFragment
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {
    fun inject(fragment: CharacterListFragment)

    fun inject(fragment: CharacterFragment)

    fun inject(fragment: EpisodeListFragment)

    fun inject(fragment: EpisodeFragment)

    fun inject(fragment: LocationListFragment)

    fun inject(fragment: LocationFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}