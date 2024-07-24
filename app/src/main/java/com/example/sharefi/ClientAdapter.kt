//package com.example.sharefi
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.sharefi.databinding.ItemClientBinding
//import com.example.direct_share.ClientInfo
//
//class ClientAdapter(private val clients: List<ClientInfo>) :
//    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {
//
//    inner class ClientViewHolder(val binding: ItemClientBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
//        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ClientViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
//        val clientInfo = clients[position]
//        holder.binding.ipAddressTextView.text = clientInfo.ipAddress
//        holder.binding.restrictSwitch.isChecked = clientInfo.isRestricted
//
//        holder.binding.restrictSwitch.setOnCheckedChangeListener { _, isChecked ->
//            clientInfo.isRestricted = isChecked
//            // Handle restriction change if needed
//        }
//
//        holder.binding.chatButton.setOnClickListener {
//            // Handle chat button click
//            // For example, navigate to a chat fragment
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return clients.size
//    }
//}
