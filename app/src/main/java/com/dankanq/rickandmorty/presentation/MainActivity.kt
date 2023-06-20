package com.dankanq.rickandmorty.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.presentation.character.main.CharactersFragment
import com.dankanq.rickandmorty.presentation.episode.main.EpisodeListFragment
import com.dankanq.rickandmorty.presentation.location.main.LocationListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var isInternetConnected = false
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var networkViewModel: NetworkViewModel

    private fun checkNetworkConnection() {
        val tvNetworkState = findViewById<TextView>(R.id.tv_network_state)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isInternetConnected = true
                runOnUiThread {
                    tvNetworkState.isVisible = false
                }
                networkViewModel.setNetworkState(true)
            }

            override fun onLost(network: Network) {
                isInternetConnected = false
                runOnUiThread {
                    tvNetworkState.isVisible = true
                    tvNetworkState.text = getString(R.string.network_state_error)
                    tvNetworkState.setBackgroundResource(R.color.dead)
                }
                networkViewModel.setNetworkState(false)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun setupBottomNavigation() {
        val fragmentManager: FragmentManager = supportFragmentManager

        val charactersFragment = CharactersFragment.newInstance()
        val episodesFragment = EpisodeListFragment.newInstance()
        val locationsFragment = LocationListFragment.newInstance()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.characters -> charactersFragment
                R.id.episodes -> episodesFragment
                R.id.locations -> locationsFragment
                else -> charactersFragment
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
            true
        }

        bottomNavigationView.selectedItemId = R.id.characters
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkViewModel = ViewModelProvider(this)[NetworkViewModel::class.java]

        checkNetworkConnection()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()

        networkViewModel.isConnected.observe(this) { isConnected ->
            isInternetConnected = isConnected
            val tvNetworkState = findViewById<TextView>(R.id.tv_network_state)
            tvNetworkState.isVisible = !isConnected
            if (!isConnected) {
                tvNetworkState.text = getString(R.string.network_state_error)
                tvNetworkState.setBackgroundResource(R.color.dead)
            }
        }

        if (isInternetConnected) {
            val tvNetworkState = findViewById<TextView>(R.id.tv_network_state)
            tvNetworkState.isVisible = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}