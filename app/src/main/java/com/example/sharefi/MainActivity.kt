package com.example.sharefi

import WiFiDirectBroadcastReceiver
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.direct_share.DirectNetShare
import com.example.sharefi.databinding.ActivityMainBinding
import com.example.sharefi.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var share: DirectNetShare
    //broadcast receiver
//    val intentFilter = IntentFilter().apply {
//        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//    }
    private val groupCreatedListener = DirectNetShare.GroupCreatedListener { ssid, password ->
        // Handle group creation event here if necessary
    }

   private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        share = DirectNetShare(this, groupCreatedListener)
        //permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_LOCATION_PERMISSION = 1001
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        //bind
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navigation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }



    /* register the broadcast receiver with the intent values to be matched */

}

fun <T : Any> T?.requireNotNull(): T {
    return this ?: throw IllegalStateException("Value cannot be null")
}