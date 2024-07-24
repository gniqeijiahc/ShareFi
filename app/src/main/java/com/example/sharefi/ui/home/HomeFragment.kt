package com.example.sharefi.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.direct_share.Constants
import com.example.direct_share.DirectNetShare
import com.example.sharefi.MainActivity
//import com.example.direct_share.DirectNetShare.ClientListListener
import com.example.sharefi.Utils
import com.example.sharefi.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
//    val wifiP2PManager by lazy {
//        requireContext().getSystemService<WifiP2pManager>().requireNotNull()
//    }
    //var channel: WifiP2pManager.Channel? = null
    //private lateinit var wifiP2pManager: WifiP2pManager
   // private lateinit var wifiP2pConfig: WifiP2pConfig
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //var receiver: BroadcastReceiver? = null
    private lateinit var ssidEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var portEditText: EditText
    private val groupCreatedListener =
        DirectNetShare.GroupCreatedListener { ssid, password ->
            ssidEditText.setText(ssid)
            passwordEditText.setText(password)
            portEditText.setText(Constants.PROXY_PORT.toString())
        }
    private lateinit var share: DirectNetShare


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

        ssidEditText = binding.ssidEditText
        passwordEditText = binding.passwordEditText
        portEditText = binding.portEditText
        val shareButton: Button = binding.shareButton
        val shareSwitch: Switch = binding.shareSwitch

        share = (requireActivity() as MainActivity).share
        share.setGroupCreatedListener(groupCreatedListener)

        shareSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            // Your code here
            if (isChecked) {
                checkWifiAndStart()
            } else {
                stopShare()
            }
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
//        share = DirectNetShare(requireActivity(), groupCreatedListener)
        share.start()
//        lifecycleScope.launch {
//            val success = rootManager.dhcpSetup()
//            Log.d("MainActivity", "DHCP setup successful? $success")
//        }
//        share.getClientList(ClientListListener { clientList ->
//            for (client in clientList) {
//                Log.i(
//                    "ClientList",
//                    "Client device: " + client.deviceName + ", " + client.deviceAddress
//                )
//            }
//        })
    }

    private fun stopShare() {
        share.stop()
        ssidEditText.setText("")
        passwordEditText.setText("")
        portEditText.setText("")
    }

}