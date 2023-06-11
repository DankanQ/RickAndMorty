package com.dankanq.rickandmorty.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.presentation.character.main.CharactersFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            launchCharactersFragment()
        }
    }

    private fun launchCharactersFragment() {
        val fragment = CharactersFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }
}