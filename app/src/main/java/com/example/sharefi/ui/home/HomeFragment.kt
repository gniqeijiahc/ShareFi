package com.example.sharefi.ui.home

//import com.example.direct_share.DirectNetShare.ClientListListener

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.sharefi.Utils
import com.example.sharefi.databinding.FragmentHomeBinding
import com.example.sharefi.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    //var receiver: BroadcastReceiver? = null
    private lateinit var ssidEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var portEditText: EditText
    private val groupCreatedListener =
        DirectNetShare.GroupCreatedListener { ssid, password ->
            ssidEditText.setText(ssid.removePrefix("DIRECT-SF-"))
            passwordEditText.setText(password)
            portEditText.setText(Constants.PROXY_PORT.toString())
        }
    private lateinit var share: DirectNetShare
    private lateinit var auth: FirebaseAuth


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
        val shareSwitch: Switch = binding.shareSwitch
        val settingButton: Button = binding.settingButton

        share = (requireActivity() as MainActivity).share
        share.setGroupCreatedListener(groupCreatedListener)
        auth = (requireActivity() as MainActivity).auth

        data class User(
            var userId: String? = null,
            var SSID: String? = null,
            var email: String? = null,
            var point: Number = 0,
            var password: String? = null,
            var isSharing: Boolean? = null
        )
        val database = FirebaseDatabase.getInstance("https://sharefi-84214-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val usersRef = database.getReference("users")
        val userId = usersRef.push().key // Generate a unique key for the user
        val user = User(userId, "John Doe", "john.doe@example.com")

        shareSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            // Your code here
            if (isChecked) {
                checkWifiAndStart()
                ssidEditText.isEnabled = false
                passwordEditText.isEnabled = false
            } else {
                stopShare()
                ssidEditText.isEnabled = true
                passwordEditText.isEnabled = true
            }

            if (userId != null) {
                usersRef.child(userId).setValue(user)
                    .addOnSuccessListener {
                        // Data successfully written
                        Log.d("Firebase", "User data written successfully.")
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firebase", "Failed to write user data.", e)
                    }
            }
        }


        settingButton.setOnClickListener{
            // Sign out the user
            auth.signOut()


            // Optional: Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Close the current activity
        }
        addTextChangeListener(ssidEditText) { newSsid ->
            share.setSsid(newSsid)
        }

        addTextChangeListener(passwordEditText) { newPassword ->
            share.setPassphrase(newPassword)
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
//        ssidEditText.setText("")
//        passwordEditText.setText("")
//        portEditText.setText("")
    }

    fun addTextChangeListener(editText: EditText, action: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                action(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

}