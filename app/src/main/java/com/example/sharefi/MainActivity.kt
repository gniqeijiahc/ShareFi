package com.example.sharefi

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sharefi.databinding.ActivityMainBinding
import com.example.sharefi.ui.home.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var wifiP2pManager: WifiP2pManager
//    private var channel: WifiP2pManager.Channel? = null
//    private lateinit var wifiP2pConfig: WifiP2pConfig
val wifiP2PManager by lazy {
    this.getSystemService<WifiP2pManager>().requireNotNull()
}
    override fun onCreate(savedInstanceState: Bundle?) {
//        private val homeViewModel: HomeViewModel by viewModels()
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_LOCATION_PERMISSION = 1001
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView



        var wifiP2pConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiP2pConfig.Builder()
                .setNetworkName("DIRECT-SF-Wifi")
                .setPassphrase("12345678")
                .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_2GHZ)
                .build()
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        val channel: WifiP2pManager.Channel = wifiP2PManager.initialize(
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
        wifiP2PManager.createGroup(
            channel,
            wifiP2pConfig,
            listener,
        )

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



}

fun <T : Any> T?.requireNotNull(): T {
    return this ?: throw IllegalStateException("Value cannot be null")
}