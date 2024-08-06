package com.example.chat

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chat.R
import com.example.chat.chatClient

class MainActivity : AppCompatActivity() {
    var ip: EditText? = null
    var port: EditText? = null
    var portText: EditText? = null
    var connectButton: Button? = null
    var showIPtextId: TextView? = null
    var showIPaddress: String? = null
    var TAG = "MAIN"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        portText = findViewById<EditText>(R.id.myPortEditText)
        ip = findViewById<EditText>(R.id.ipEditText)
        port = findViewById<EditText>(R.id.portEditText)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val showIPtextId = findViewById<TextView>(R.id.showIPtextId)
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        showIPaddress = Formatter.formatIpAddress(wm.connectionInfo.getIpAddress())
        showIPtextId.setText(showIPaddress)
        connectButton.setOnClickListener(View.OnClickListener { view: View? ->
            if (Patterns.IP_ADDRESS.matcher(ip?.getText()).matches()) {
                val info = info
                val intent = Intent(
                    this@MainActivity,
                    chatClient::class.java
                )
                intent.putExtra("ip&port", info)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            } else {
                val toast =
                    Toast.makeText(this, "Please Enter a Valid IP Address", Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    val info: String
        get() {
            val info = ip!!.getText().toString() + " " + port!!.getText()
                .toString() + " " + portText!!.getText().toString()
            Log.i(TAG, "info => $info")
            return info
        }
}
