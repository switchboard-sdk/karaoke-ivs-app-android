package com.synervoz.switchboardsampleapp.karaokewithivs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardamazonivs.AmazonIVSExtension
import com.synervoz.switchboardaudioeffects.AudioEffectsExtension
import com.synervoz.switchboardsampleapp.karaokewithivs.broadcast.ui.BroadcastScreen
import com.synervoz.switchboardsampleapp.karaokewithivs.client.ui.ClientScreen
import com.synervoz.switchboardsampleapp.karaokewithivs.config.superpoweredLicenseKey
import com.synervoz.switchboardsampleapp.karaokewithivs.config.switchboardClientID
import com.synervoz.switchboardsampleapp.karaokewithivs.config.switchboardClientSecret
import com.synervoz.switchboardsampleapp.karaokewithivs.guide.GuideScreen
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.ui.RealtimeScreen
import com.synervoz.switchboardsampleapp.karaokewithivs.settings.SettingsScreen
import com.synervoz.switchboardsampleapp.karaokewithivs.ui.theme.KaraokeWithIVSTheme
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.ContextHolder
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import com.synervoz.switchboardsuperpowered.SuperpoweredExtension

private enum class Screen { LIST, REALTIME, CLIENT, BROADCAST, SETTINGS, GUIDE }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        ContextHolder.activity = this
        PreferenceManager()

        // Load the extensions, then initialize with all three listed so their v3 JSON node factories
        // register (Superpowered.*, AudioEffects.*, AmazonIVS.Sink).
        SuperpoweredExtension.load()
        AudioEffectsExtension.load()
        AmazonIVSExtension.load()
        Switchboard.initialize(
            this,
            switchboardClientID,
            switchboardClientSecret,
            mapOf(
                "Superpowered" to mapOf("superpoweredLicenseKey" to superpoweredLicenseKey),
                "AudioEffects" to emptyMap<String, Any>(),
                "AmazonIVS" to emptyMap<String, Any>(),
            ),
        )

        setContent {
            KaraokeWithIVSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PermissionGate { AppNavigation() }
                }
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    var screen by remember { mutableStateOf(Screen.LIST) }
    val back = { screen = Screen.LIST }
    when (screen) {
        Screen.LIST -> ExampleList(onSelect = { screen = it })
        Screen.REALTIME -> RealtimeScreen(onBack = back)
        Screen.CLIENT -> ClientScreen(onBack = back)
        Screen.BROADCAST -> BroadcastScreen(onBack = back)
        Screen.SETTINGS -> SettingsScreen(onBack = back)
        Screen.GUIDE -> GuideScreen(onBack = back)
    }
}

@Composable
private fun ExampleList(onSelect: (Screen) -> Unit) {
    val examples = listOf(
        "Karaoke with Real-Time IVS (Stage)" to Screen.REALTIME,
        "Real-Time IVS Listener" to Screen.CLIENT,
        "Karaoke with Broadcast IVS" to Screen.BROADCAST,
        "Settings" to Screen.SETTINGS,
        "Guide" to Screen.GUIDE,
    )
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Karaoke with IVS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        examples.forEach { (title, target) ->
            Button(onClick = { onSelect(target) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(title)
            }
        }
    }
}

@Composable
private fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val permissions = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    fun granted() = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    var hasPermissions by remember { mutableStateOf(granted()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermissions = granted()
    }
    LaunchedEffect(Unit) {
        if (!hasPermissions) launcher.launch(permissions.toTypedArray())
    }

    if (hasPermissions) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Microphone and camera permissions are required.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { launcher.launch(permissions.toTypedArray()) }) { Text("Grant permissions") }
        }
    }
}
