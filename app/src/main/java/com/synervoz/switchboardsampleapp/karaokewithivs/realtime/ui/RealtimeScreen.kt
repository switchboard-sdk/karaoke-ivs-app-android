package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.ui

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio.KaraokeWithIVSRealtimeExample
import com.synervoz.switchboardsampleapp.karaokewithivs.ui.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SONG = "House_of_the_Rising_Sun.mp3"

/** Real-time (IVS Stage) karaoke example — sing over a beat with effects and publish to an IVS Stage. */
@Composable
fun RealtimeScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val engine = remember { KaraokeWithIVSRealtimeExample(context.applicationContext) }

    var loading by remember { mutableStateOf(true) }
    var isStreaming by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPlayingRecording by remember { mutableStateOf(false) }
    var monitorVoice by remember { mutableStateOf(false) }
    var musicVolume by remember { mutableStateOf(50f) }
    var voiceVolume by remember { mutableStateOf(100f) }
    var vibrato by remember { mutableStateOf(false) }
    var chorus by remember { mutableStateOf(false) }
    var flanger by remember { mutableStateOf(false) }
    var delay by remember { mutableStateOf(false) }
    var reverb by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(0.0) }
    var position by remember { mutableStateOf(0.0) }
    var progress by remember { mutableStateOf(0f) }
    var micPeak by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        onDispose { engine.stopAudioGraph() }
    }
    LaunchedEffect(Unit) {
        engine.startAudioGraph()
        withContext(Dispatchers.IO) { engine.loadSong(SONG) }
        duration = engine.getSongDurationInSeconds()
        loading = false
    }
    LaunchedEffect(Unit) {
        while (true) {
            if (!loading) {
                if (isPlaying) { position = engine.getPositionInSeconds(); progress = engine.getProgress() }
                micPeak = engine.vuPeak
            }
            withFrameMillis { it }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Text("Karaoke with Real-Time IVS", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        Text("${formatTime(position)} / ${formatTime(duration)}", fontSize = 14.sp, modifier = Modifier.align(Alignment.End))
        Text("Mic level", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        LinearProgressIndicator(progress = { micPeak }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))

        Button(
            onClick = { if (isPlaying) engine.pauseMusic() else engine.playMusic(); isPlaying = !isPlaying },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) { Text(if (isPlaying) "Pause music" else "Play music") }

        Text("Voice volume", fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
        Slider(value = voiceVolume, onValueChange = { voiceVolume = it; engine.setVoiceVolume(it.toInt()) }, valueRange = 0f..100f)
        Text("Music volume", fontSize = 15.sp)
        Slider(value = musicVolume, onValueChange = { musicVolume = it; engine.setMusicVolume(it.toInt()) }, valueRange = 0f..100f)

        EffectRow("Monitor my voice", monitorVoice) { monitorVoice = it; engine.setMonitorVoice(it) }

        Text("Effects", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        EffectRow("Vibrato", vibrato) { vibrato = it; engine.setVibratoEnabled(it) }
        EffectRow("Chorus", chorus) { chorus = it; engine.setChorusEnabled(it) }
        EffectRow("Flanger", flanger) { flanger = it; engine.setFlangerEnabled(it) }
        EffectRow("Delay", delay) { delay = it; engine.setDelayEnabled(it) }
        EffectRow("Reverb", reverb) { reverb = it; engine.setReverbEnabled(it) }

        Button(
            onClick = { if (isStreaming) engine.stopStream() else engine.startStream(); isStreaming = !isStreaming },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) { Text(if (isStreaming) "Stop Streaming" else "Start Streaming") }

        if (!isStreaming) {
            Button(
                onClick = {
                    if (isPlayingRecording) engine.stopRecordingPlayback() else engine.playRecording()
                    isPlayingRecording = !isPlayingRecording
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) { Text(if (isPlayingRecording) "Stop Recording" else "Play Recording") }
        }
    }
}

@Composable
private fun EffectRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
