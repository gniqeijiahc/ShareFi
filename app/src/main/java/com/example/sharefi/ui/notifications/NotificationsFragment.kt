package com.example.sharefi.ui.notifications

import FileServerAsyncTask
import UdpPacketListener
import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.RouteInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sharefi.R
import com.example.sharefi.databinding.FragmentNotificationsBinding
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class NotificationsFragment : Fragment() {
    val FILE_PICKER_REQUEST_CODE = 123
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

//        val statusText: TextView = binding.statusText

        val filePickerButton: Button = root.findViewById(R.id.filePickerButton)
        filePickerButton.setOnClickListener {
            openFilePicker()
        }

        val sendButton: Button = root.findViewById(R.id.buttonSend)
        sendButton.setOnClickListener {
//            clientSendFile(textView.toString())
            Thread {
                try {
                    val message = "Hello, this is a test message."
                    val sendData = message.toByteArray()
                    val address = InetAddress.getByName("127.0.0.1") // 替换为接收方的IP地址
                    val port = 8228 // 替换为接收方监听的端口号
                    val packet = DatagramPacket(sendData, sendData.size, address, port)
                    val socket = DatagramSocket()
                    socket.send(packet)
                    socket.close()
                    Log.d("Client", "Sent message: $message")
                } catch (e: IOException) {
                    Log.e("Client", "Error sending message: ${e.message}")
                }
            }.start()
        }

//        val receiveButton: Button = root.findViewById(R.id.buttonReceive)
//        receiveButton.setOnClickListener {
////            openServer(statusText)
//            val udpListener = UdpPacketListener()
//            udpListener.start()
//        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    private fun openServer(statusText: TextView){
        var fileServer = FileServerAsyncTask(requireContext(), statusText)
        fileServer.run {  }

    }

    private fun clientSendFile(filePath : String){
        val context = requireContext()
        val host: String
        val port: Int
        var len: Int
        val socket = Socket()
        val buf = ByteArray(1024)

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */

//            val connectivityManager = getSystemService(context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val defaultRoute = connectivityManager.activeNetwork?.let { network ->
//                connectivityManager.getLinkProperties(network)?.defaultRoute
//            }
//            Log.d("DefaultRoute", defaultRoute?.toString() ?: "Unknown")

            WifiManager.EXTRA_NETWORK_INFO
            socket.bind(null)
            socket.connect((InetSocketAddress("192.168.49.157", 8228)), 500)

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data is retrieved by the server device.
             */
            val outputStream = socket.getOutputStream()
            val cr = context.contentResolver
            val inputStream: InputStream? = cr.openInputStream(Uri.parse(filePath))
            if (inputStream != null) {
                while (inputStream.read(buf).also { len = it } != -1) {
                    outputStream.write(buf, 0, len)
                }
            }
            outputStream.close()
            inputStream?.close()
        } catch (e: FileNotFoundException) {
            //catch logic
        } catch (e: IOException) {
            //catch logic
        } finally {
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            socket.takeIf { it.isConnected }?.apply {
                close()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val filePath = uri.toString()
                val filePathTextView: TextView = binding.textNotifications
                filePathTextView.text = filePath
            }
        }
    }
}