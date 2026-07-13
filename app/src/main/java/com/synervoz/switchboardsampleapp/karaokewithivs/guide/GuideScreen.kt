package com.synervoz.switchboardsampleapp.karaokewithivs.guide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Guide — how to test the IVS karaoke app end to end. */
@Composable
fun GuideScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Guide", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("To test the IVS Karaoke sample app end-to-end you need two Android phones: one to broadcast/publish and one to listen.")
        Text("As the IVS session is recorded, you can hear the voice effects locally as well. When you Stop Streaming you can play the recorded session.")
        Text("If the volume of the voice is low, please try singing into the top or the bottom microphone of the device.")
        Text("You can use your own IVS Stage tokens for both Publisher and Listener in Settings.")
    }
}
