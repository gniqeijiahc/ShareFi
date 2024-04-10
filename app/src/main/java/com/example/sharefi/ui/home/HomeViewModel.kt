package com.example.sharefi.ui.home

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    lateinit var channel : WifiP2pManager.Channel
    lateinit var wifiP2pConfig: WifiP2pConfig
    lateinit var wifiP2pManager: WifiP2pManager
}