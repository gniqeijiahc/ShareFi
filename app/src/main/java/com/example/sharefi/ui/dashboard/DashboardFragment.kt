package com.example.sharefi.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.direct_share.ClientInfo
import com.example.direct_share.DirectNetShare
import com.example.sharefi.MainActivity
import com.example.sharefi.databinding.FragmentDashboardBinding
import com.example.sharefi.databinding.ItemClientBinding




class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private lateinit var clientAdapter: ClientAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var share: DirectNetShare

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
        val clients = hashMapOf(
            "192.168.1.2" to ClientInfo("192.168.1.2"),
            "192.168.1.3" to ClientInfo("192.168.1.3"),
            "192.168.1.4" to ClientInfo("192.168.1.4")
        )


//        clientAdapter = ClientAdapter(share.getClientInfo())
        clientAdapter = ClientAdapter(clients)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = clientAdapter

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
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