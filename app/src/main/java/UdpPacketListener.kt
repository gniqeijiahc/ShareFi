import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class UdpPacketListener : Thread() {
    private val port = 8228
    private val bufferSize = 1024
    private var isRunning = false

    override fun run() {
        val buffer = ByteArray(bufferSize)
        val socket = DatagramSocket(port)
        isRunning = true
        while (isRunning) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                Log.d("UdpPacketListener", "Received UDP packet: $message")
            } catch (e: SocketException) {
                // Socket closed, thread is stopping
                isRunning = false
            }
        }
        socket.close()
    }

    fun stopListening() {
        isRunning = false
    }
}