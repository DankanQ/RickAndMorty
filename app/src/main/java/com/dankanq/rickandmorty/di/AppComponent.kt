package com.dankanq.rickandmorty.di

import android.app.Application
import com.dankanq.rickandmorty.presentation.character.main.CharactersFragment
import com.dankanq.rickandmorty.presentation.character.detail.CharacterFragment
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
    fun inject(fragment: CharactersFragment)

    fun inject(fragment: CharacterFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}