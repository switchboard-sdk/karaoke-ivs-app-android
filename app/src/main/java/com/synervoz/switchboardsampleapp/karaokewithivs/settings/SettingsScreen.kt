package com.synervoz.switchboardsampleapp.karaokewithivs.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager

/** Settings — the IVS ingest server / stream key / publisher & listener Stage tokens. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    var publisherToken by remember { mutableStateOf(PreferenceManager.getGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN)) }
    var listenerToken by remember { mutableStateOf(PreferenceManager.getGlobalStringPreference(PreferenceConstants.CLIENT_TOKEN)) }
    var streamKey by remember { mutableStateOf(PreferenceManager.getGlobalStringPreference(PreferenceConstants.STREAM_KEY)) }
    var ingestServer by remember { mutableStateOf(PreferenceManager.getGlobalStringPreference(PreferenceConstants.INGEST_SERVER)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Realtime IVS Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(publisherToken, { publisherToken = it }, label = { Text("Publisher Token") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(listenerToken, { listenerToken = it }, label = { Text("Listener Token") }, modifier = Modifier.fillMaxWidth())

        Text("Broadcast IVS Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(streamKey, { streamKey = it }, label = { Text("Stream Key") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ingestServer, { ingestServer = it }, label = { Text("Ingest Server") }, modifier = Modifier.fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { publisherToken = ""; listenerToken = ""; streamKey = ""; ingestServer = "" },
                modifier = Modifier.weight(1f),
            ) { Text("Clear All") }
            Button(
                onClick = {
                    PreferenceManager.setGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN, publisherToken)
                    PreferenceManager.setGlobalStringPreference(PreferenceConstants.CLIENT_TOKEN, listenerToken)
                    PreferenceManager.setGlobalStringPreference(PreferenceConstants.STREAM_KEY, streamKey)
                    PreferenceManager.setGlobalStringPreference(PreferenceConstants.INGEST_SERVER, ingestServer)
                    onBack()
                },
                modifier = Modifier.weight(1f),
            ) { Text("Save") }
        }
    }
}
