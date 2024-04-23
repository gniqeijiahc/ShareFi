package com.example.sharefi.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.direct_share.DirectNetShare
import com.example.sharefi.MainActivity
import com.example.sharefi.Utils
import com.example.sharefi.databinding.FragmentHomeBinding
import com.example.sharefi.requireNotNull

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val wifiP2PManager by lazy {
        requireContext().getSystemService<WifiP2pManager>().requireNotNull()
    }
    var channel: WifiP2pManager.Channel? = null
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var wifiP2pConfig: WifiP2pConfig
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var receiver: BroadcastReceiver? = null
    lateinit var infoTv: TextView
    private val groupCreatedListener = DirectNetShare.GroupCreatedListener { ssid, password -> infoTv.text = String.format("SSID : %s\nPassword : %s", ssid, password) }
    private lateinit var share: DirectNetShare
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            wifiP2pManager = context.wifiP2pManager
            channel = context.channel

//            receiver = context.receiver
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        infoTv = binding.infoTv
        val shareButton: Button = binding.shareButton
        val shareSwitch: Switch = binding.shareSwitch
        shareSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            // Your code here
            if (isChecked) {
                checkWifiAndStart()
            } else {
                stopShare()
            }
        }
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


//        val wifiP2pConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            WifiP2pConfig.Builder()
//                .setNetworkName("DIRECT-SF-Wifi")
//                .setPassphrase("12345678")
//                .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_2GHZ)
//                .build()
//        } else {
//            TODO("VERSION.SDK_INT < Q")
//        }
//        val channel: WifiP2pManager.Channel = wifiP2PManager.initialize(
//            requireContext(),
//            Looper.getMainLooper(),
//        ) {
//            // Before we used to kill the Network
//            //
//            // But now we do nothing - if you Swipe Away the app from recents,
//            // the p2p manager will die, but when it comes back we want everything to
//            // attempt to run again so we leave this around.
//            //
//            // Any other unexpected death like Airplane mode or Wifi off should be covered by the receiver
//            // so we should never unintentionally leak the service
//            Log.d("YourTag", "WifiP2PManager Channel died! Do nothing :D")
//        }
//        val listener =
//            object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Log.d("WifiP2P", "Group created successfully")
//                    Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onFailure(reason: Int) {
//                    Log.d("WifiP2P", "Failed to create group. Reason: $reason")
//                    Toast.makeText(requireContext(), "fail", Toast.LENGTH_SHORT).show()
//                }
//            }


        shareButton.setOnClickListener {

//            if (shareButton.text == "Share") {
//                shareButton.text = "Pressed"
//
//                if (ActivityCompat.checkSelfPermission(
//                        requireContext(),
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                        requireContext(),
//                        Manifest.permission.NEARBY_WIFI_DEVICES
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    val REQUEST_LOCATION_PERMISSION = 1001
//                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
//                }
//
//                channel?.let{channel ->
//                    wifiP2PManager.createGroup(
//                        channel,
//                        wifiP2pConfig,
//                        listener,
//                    )
//                }
//
//            } else {
//                shareButton.text = "Share"
//
//                channel?.let{channel ->
//                    wifiP2PManager.removeGroup(
//                        channel,
//                        listener
//                    )
//                }
//            }
//

//            channel?.also { channel ->
//                receiver = WiFiDirectBroadcastReceiver(wifiP2PManager, channel, requireActivity())
//            }

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkWifiAndStart() {
        if (!Utils.isWifiEnabled(requireContext())) {
//            registerReceiver(requireContext(),
//                object : BroadcastReceiver() {
//                override fun onReceive(context: Context, intent: Intent) {
//                    if (intent.action != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
//                        val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
//                        if (!noConnectivity) {
//                            startShare()
//                            unregisterReceiver(this)
//                        }
//                    }
//                }
//            }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
//                ComponentActivity.RECEIVER_EXPORTED,
//            )
//            Utils.enableWifi(requireContext())
        } else {
            startShare()
        }
    }

    private fun startShare() {
        share = DirectNetShare(requireActivity(), groupCreatedListener)
        share.start()
//        lifecycleScope.launch {
//            val success = rootManager.dhcpSetup()
//            Log.d("MainActivity", "DHCP setup successful? $success")
//        }
    }

    private fun stopShare() {
        share.stop()
        //infoTv.text = ""
    }

}