package com.dankanq.rickandmorty

import android.app.Application
import com.dankanq.rickandmorty.di.DaggerAppComponent

class RickAndMortyApp: Application() {
    val component by lazy {
        DaggerAppComponent.factory()
            .create(this)
    }
}