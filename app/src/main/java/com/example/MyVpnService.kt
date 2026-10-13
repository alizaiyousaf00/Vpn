package com.example

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    companion object {
        private val _connectionState = MutableStateFlow("Disconnected")
        val connectionState: StateFlow<String> = _connectionState
        
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startVpn()
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        _connectionState.value = "Connecting"

        thread(start = true) {
            try {
                // Configure the VPN Interface based on the provided requirements
                val builder = Builder()
                
                builder.setSession("Lightweight VPN")
                // Assign a local IPv4 address
                builder.addAddress("10.0.0.2", 32)
                
                // Set custom DNS routing to bypass ISP blocks
                builder.addDnsServer("1.1.1.1") // Cloudflare
                builder.addDnsServer("8.8.8.8") // Google
                
                // Add full route to capture all traffic
                builder.addRoute("0.0.0.0", 0)
                
                // Configure custom MTU size to avoid packet fragmentation/drops
                builder.setMtu(1280)

                // Establish the VPN interface
                vpnInterface = builder.establish()
                
                if (vpnInterface != null) {
                    _connectionState.value = "Connected"
                    isRunning = true

                    // Dummy local read/write loop to keep the VpnService alive and active.
                    // Note: In a fully functional remote VPN (like OpenVPN/WireGuard), 
                    // packets from this input stream would be encrypted and sent to a remote server socket.
                    val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
                    val outputStream = FileOutputStream(vpnInterface?.fileDescriptor)
                    val packet = ByteBuffer.allocate(1280)

                    while (isRunning) {
                        try {
                            // Yielding thread or minimal read to keep loop active
                            Thread.sleep(100) 
                            
                            // A standard implementation would do:
                            // val length = inputStream.read(packet.array())
                            // if (length > 0) { ... process packet ... }
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                } else {
                    _connectionState.value = "Disconnected"
                }
            } catch (e: Exception) {
                Log.e("MyVpnService", "Error starting VPN", e)
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error closing VPN interface", e)
        }
        vpnInterface = null
        _connectionState.value = "Disconnected"
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
