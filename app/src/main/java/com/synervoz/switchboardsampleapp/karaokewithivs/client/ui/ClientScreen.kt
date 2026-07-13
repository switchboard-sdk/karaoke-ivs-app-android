package com.synervoz.switchboardsampleapp.karaokewithivs.client.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.synervoz.switchboardsampleapp.karaokewithivs.client.audio.KaraokeWithIVSRealtimeClientExample

/** Real-Time IVS Listener — subscribes to an IVS Stage as an audio-only participant (pure IVS, no Switchboard graph). */
@Composable
fun ClientScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val example = remember { KaraokeWithIVSRealtimeClientExample(context) }
    var listening by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { if (listening) example.stopStream() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Real-Time IVS Listener", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Joins the IVS Stage with the Listener token from Settings and plays the broadcast.")
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { if (listening) example.stopStream() else example.startStream(); listening = !listening },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (listening) "Stop Listening" else "Start Listening") }
    }
}
