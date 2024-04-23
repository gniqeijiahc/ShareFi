import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.sharefi.MainActivity

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                val peerListListener = object : WifiP2pManager.PeerListListener {
                    override fun onPeersAvailable(peers: WifiP2pDeviceList) {
                        // 获取对等设备列表
                        val peerList = peers.deviceList
                        for (device in peerList) {
                            // 处理每个对等设备的信息
                            Log.d("Device List", "Device Name: ${device.deviceName}")
                            Log.d("Device List", "Device Address: ${device.deviceAddress}")
                            // 可以根据需要添加其他信息
                        }
                    }
                }
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                manager.requestPeers(channel, peerListListener)
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

                val peerListListener = object : WifiP2pManager.PeerListListener {
                    override fun onPeersAvailable(peers: WifiP2pDeviceList) {
                        // 获取对等设备列表
                        val peerList = peers.deviceList
                        for (device in peerList) {
                            // 处理每个对等设备的信息
                            Log.d("Device List", "Device Name: ${device.deviceName}")
                            Log.d("Device List", "Device Address: ${device.deviceAddress}")
                            // 可以根据需要添加其他信息
                        }
                    }
                }
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                manager.requestPeers(channel, peerListListener)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }
}