package com.example

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.EmeraldBg
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoBg
import com.example.ui.theme.IndigoBorder
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BackgroundDark
                ) { innerPadding ->
                    VpnScreen(
                        modifier = Modifier.padding(innerPadding),
                        onConnect = { prepareAndStartVpn() },
                        onDisconnect = { stopVpnService() }
                    )
                }
            }
        }
    }

    private fun prepareAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private val vpnPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_CONNECT
        }
        startService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_DISCONNECT
        }
        startService(intent)
    }
}

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val connectionState by MyVpnService.connectionState.collectAsState()
    val isConnected = connectionState == "Connected"
    val isConnecting = connectionState == "Connecting"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(IndigoAccent, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "NetShield VPN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            }
            IconButton(onClick = { /* Settings */ }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .background(EmeraldBg, CircleShape)
                            .border(1.dp, EmeraldBorder, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SECURE CONNECTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldSuccess,
                            letterSpacing = 1.5.sp
                        )
                    }
                } else if (isConnecting) {
                     Box(
                        modifier = Modifier
                            .background(IndigoBg, CircleShape)
                            .border(1.dp, IndigoBorder, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CONNECTING...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndigoAccent,
                            letterSpacing = 1.5.sp
                        )
                    }
                } else {
                     Box(
                        modifier = Modifier
                            .background(Color(0x1A64748B), CircleShape)
                            .border(1.dp, Color(0x3364748B), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "UNPROTECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                
                Text(
                    text = if (isConnected) "Connected" else if (isConnecting) "Connecting" else "Disconnected",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = if (isConnected) "Your browsing is now encrypted" else "Connect to secure your browsing",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Huge Connect Button
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                if (isConnected || isConnecting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(48.dp)
                            .background(
                                color = if (isConnected) EmeraldSuccess.copy(alpha = 0.2f) else IndigoAccent.copy(alpha = 0.2f), 
                                shape = CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .background(SurfaceDark, CircleShape)
                        .border(4.dp, SurfaceVariantDark, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (isConnected || isConnecting) onDisconnect() else onConnect()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .background(
                                if (isConnected) EmeraldBg else if (isConnecting) IndigoBg else Color(0x0DFFFFFF),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isConnected) EmeraldBorder else if (isConnecting) IndigoBorder else Color(0x1AFFFFFF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Power",
                            modifier = Modifier.size(48.dp),
                            tint = if (isConnected) EmeraldSuccess else if (isConnecting) IndigoLight else TextSecondary
                        )
                    }
                }
            }

            // Info Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Server Location
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceDark, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SERVER LOCATION",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp, 12.dp)
                                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "Frankfurt, DE" else "Auto Select",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                // IP Address
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceDark, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "IP ADDRESS",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (isConnected) "185.122.41.92" else "---.---.---.---",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(SurfaceDark)
                .border(1.dp, Color(0x0DFFFFFF))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "MTU SIZE", fontSize = 10.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "1280", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = IndigoLight)
                }
                
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0x0DFFFFFF)))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "DNS", fontSize = 10.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "1.1.1.1", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = IndigoLight)
                }
                
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0x0DFFFFFF)))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "PING", fontSize = 10.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if (isConnected) "24ms" else "--", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = IndigoLight)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress bar indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF1E293B), CircleShape)
            ) {
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .height(6.dp)
                            .background(IndigoAccent, CircleShape)
                            // blur/glow hack
                            .border(1.dp, IndigoAccent.copy(alpha = 0.5f), CircleShape)
                    )
                } else if (isConnecting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .height(6.dp)
                            .background(IndigoLight, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "OPTIMIZED LINK RELOADING", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary, letterSpacing = (-0.5).sp)
                Text(text = "VPNSERVICE ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary, letterSpacing = (-0.5).sp)
            }
        }
    }
}
