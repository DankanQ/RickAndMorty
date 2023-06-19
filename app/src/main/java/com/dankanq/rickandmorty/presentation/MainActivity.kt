package com.dankanq.rickandmorty.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.presentation.character.main.CharactersFragment
import com.dankanq.rickandmorty.presentation.episode.main.EpisodeListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentManager: FragmentManager = supportFragmentManager

        val charactersFragment = CharactersFragment.newInstance()
        val episodesFragment = EpisodeListFragment.newInstance()
        // val locationsFragment = LocationListFragment.newInstance()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.characters -> fragment = charactersFragment
                R.id.episodes -> fragment = episodesFragment
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
            true
        }

        bottomNavigationView.selectedItemId = R.id.characters
    }
}