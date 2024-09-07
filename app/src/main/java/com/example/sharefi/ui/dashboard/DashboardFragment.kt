package com.example.sharefi.ui.dashboard

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.chatClient
import com.example.direct_share.ClientInfo
import com.example.direct_share.DirectNetShare
import com.example.sharefi.MainActivity
import com.example.sharefi.R
import com.example.sharefi.databinding.FragmentDashboardBinding
import com.example.sharefi.databinding.ItemClientBinding
import com.example.sharefi.ui.home.HomeFragment
import com.example.sharefi.ui.notifications.NotificationsFragment
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var enableButton: MaterialButton
    private lateinit var enableButton2: MaterialButton
    private lateinit var enableButton3: MaterialButton
    private lateinit var clientAdapter: ClientAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var share: DirectNetShare

    private val database = FirebaseDatabase.getInstance("https://sharefi-84214-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val usersRef = database.getReference("users")
    private lateinit var user: HomeFragment.User
    private var userList = mutableListOf<HomeFragment.User>()
//    val info: String
//        get() {
//            val info = ip!!.getText().toString() + " " + port!!.getText()
//                .toString() + " " + portText!!.getText().toString()
//            Log.i(TAG, "info => $info")
//            return info
//        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        share = (requireActivity() as MainActivity).share
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userList = mutableListOf<HomeFragment.User>()

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(HomeFragment.User::class.java)

                        user?.let { userList.add(it) }
                    }


                } else {
                    Log.d("Firebase", "No users found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        val chatButton: Button = binding.chatButton
        val clientInfo = share.getClientInfo().values.toList()
        var ip = "192.168.49.1"
        if(clientInfo.isNotEmpty()){
            ip = clientInfo[0].ipAddress
        }
        binding.ipAddressTextView.text = ip
        val info = ip.toString() + " " + "5000" + " " + "5000"
        Log.i(TAG, "info => $info")

        chatButton.setOnClickListener {
            val intent = Intent(
                requireActivity(),
                chatClient::class.java
            )
//                Intent(requireActivity(), com.example.chat.chatClient::class.java)
            intent.putExtra("ip&port", info)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        val chatAiButton : Button = binding.chatAi
        chatAiButton.setOnClickListener {
            val intent = Intent(
                requireActivity(),
                chatClient::class.java
            )
            intent.putExtra("ip&port", "0.0.0.0 0 0")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

    enableButton = binding.enableButton
    enableButton2 = binding.enableButton2
    enableButton3 = binding.enableButton3



// 为每个按钮设置点击监听器
    enableButton.setOnClickListener { toggleButtonState(enableButton) }
    enableButton2.setOnClickListener { toggleButtonState(enableButton2) }
    enableButton3.setOnClickListener { toggleButtonState(enableButton3) }



        return root
    }


    private fun toggleButtonState(button: MaterialButton) {
        val isEnabled = button.text.toString() == "enable"

        if (isEnabled) {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.gray))
            button.text = "disable"
            // 假设你已经有了 icon_disable 图标资源
            button.setIconResource(R.drawable.icon_disable)
        } else {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.dark_blue))
            button.text = "enable"
            button.setIconResource(R.drawable.icon_enable)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ClientAdapter(private val clients: HashMap<String, ClientInfo>) :
        RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

        inner class ClientViewHolder(val binding: ItemClientBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
            val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ClientViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
            val clientInfo = clients.values.toList()[position]
            holder.binding.ipAddressTextView.text = clientInfo.ipAddress
            holder.binding.restrictSwitch.isChecked = clientInfo.isRestricted

            holder.binding.restrictSwitch.setOnCheckedChangeListener { _, isChecked ->
                clientInfo.isRestricted = isChecked
                // Handle restriction change
            }

            holder.binding.chatButton.setOnClickListener {
                // Navigate to chat fragment
            }
        }

        override fun getItemCount(): Int {
            return clients.size
        }
    }
}