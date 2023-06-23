package com.dankanq.rickandmorty.utils.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class NetworkViewModel @Inject constructor() : ViewModel() {
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun setNetworkState(isConnected: Boolean) {
        _isConnected.postValue(isConnected)
    }
}