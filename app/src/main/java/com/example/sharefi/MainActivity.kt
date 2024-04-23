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
import com.example.sharefi.databinding.ActivityMainBinding
import com.example.sharefi.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var wifiP2pManager: WifiP2pManager
   // val wifiManager = this.getSystemService<WifiManager>().requireNotNull()

//    public lateinit var wifiP2pConfig: WifiP2pConfig
    lateinit var receiver : BroadcastReceiver
    var channel: WifiP2pManager.Channel? = null
    val wifiP2pManager by lazy {
        this.getSystemService<WifiP2pManager>().requireNotNull()
    }
//    val receiver : BroadcastReceiver = null

    //broadcast receiver
    val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

   private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channel = wifiP2pManager.initialize(
            this,
            Looper.getMainLooper(),
        ) {
            // Before we used to kill the Network
            //
            // But now we do nothing - if you Swipe Away the app from recents,
            // the p2p manager will die, but when it comes back we want everything to
            // attempt to run again so we leave this around.
            //
            // Any other unexpected death like Airplane mode or Wifi off should be covered by the receiver
            // so we should never unintentionally leak the service
            Log.d("YourTag", "WifiP2PManager Channel died! Do nothing :D")
        }

        receiver = channel?.let {
            WiFiDirectBroadcastReceiver(wifiP2pManager, it, this)
        } ?: throw IllegalStateException("Channel is null")

        //permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_LOCATION_PERMISSION = 1001
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        //bind
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //default route ip
       val defaultRouteIpAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           getWifiDefaultRouteIpAddress(this)
       } else {
           TODO("VERSION.SDK_INT < Q")
       }
       Log.d("DefaultRoute", defaultRouteIpAddress ?: "Unknown")

        //create group
//        var wifiP2pConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            WifiP2pConfig.Builder()
//                .setNetworkName("DIRECT-SF-Wifi")
//                .setPassphrase("12345678")
//                .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_2GHZ)
//                .build()
//        } else {
//            TODO("VERSION.SDK_INT < Q")
//        }

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

    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val listener =
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("WifiP2P", "Group created successfully")
                    Toast.makeText(this@MainActivity, "success", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.d("WifiP2P", "Failed to create group. Reason: $reason")
                    Toast.makeText(this@MainActivity, "fail", Toast.LENGTH_SHORT).show()
                }
            }
        wifiP2pManager.removeGroup(channel, listener)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getWifiDefaultRouteIpAddress(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
        return linkProperties.routes
            .filter { routeInfo -> routeInfo.isDefaultRoute && routeInfo.hasGateway() }
            .mapNotNull { routeInfo -> routeInfo.gateway }
            .firstOrNull()?.hostAddress
    }


    /* register the broadcast receiver with the intent values to be matched */

}

fun <T : Any> T?.requireNotNull(): T {
    return this ?: throw IllegalStateException("Value cannot be null")
}