package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.DeviceDiscovery
import com.amazonaws.ivs.broadcast.LocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardamazonivs.AmazonIVSExtension
import com.synervoz.switchboardamazonivs.audioInterface.IVSInterface
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.DialogHelper
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import java.io.File

/**
 * Real-time (IVS Stage) karaoke example on the SwitchboardSDK v3 JSON graph API.
 *
 * The microphone runs through a harmonizer (a `SubgraphProcessor` of pitch-correction + dual pitch
 * shift) and a Vibrato/Chorus/Flanger/Echo/Reverb chain, is mixed with a looping backing track, and
 * the mix is sent to an Amazon IVS Stage via `AmazonIVS.Sink` and recorded. The IVS Stage audio
 * device is registered as the shared IVS audio bus (SWI-6647).
 */
class KaraokeWithIVSRealtimeExample(val context: Context) {

    companion object {
        val TAG: String = this::class.java.name

        private const val MUSIC_PLAYER = "musicPlayer"
        private const val RECORDING_PLAYER = "recordingPlayer"
        private const val MUSIC_GAIN = "musicGain"
        private const val VOICE_GAIN = "voiceGain"
        private const val VOLUME_OUTPUT_GAIN = "volumeOutputGain"
        private const val VIBRATO = "vibrato"
        private const val CHORUS = "chorus"
        private const val FLANGER = "flanger"
        private const val DELAY = "delay"
        private const val REVERB = "reverb"
        private const val VU_METER = "vuMeter"

        private val GRAPH_JSON = """
            {
              "type": "Realtime",
              "config": {
                "microphoneEnabled": true,
                "sampleRate": 48000,
                "graph": {
                  "nodes": [
                    { "id": "musicPlayer", "type": "AudioPlayer", "config": { "isLoopingEnabled": true } },
                    { "id": "recordingPlayer", "type": "AudioPlayer", "config": { "isLoopingEnabled": true } },
                    { "id": "recorder", "type": "Recorder", "config": { "numberOfRecordedChannels": 1, "recordingSampleRate": 48000 } },
                    { "id": "musicGain", "type": "Gain" },
                    { "id": "voiceGain", "type": "Gain" },
                    { "id": "volumeOutputGain", "type": "Gain" },
                    {
                      "id": "harmonizer", "type": "SubgraphProcessor",
                      "config": { "graph": {
                        "nodes": [
                          { "id": "h_avpc", "type": "Superpowered.AutomaticVocalPitchCorrection", "config": { "enabled": false } },
                          { "id": "h_split", "type": "BusSplitter" },
                          { "id": "h_lowPitch", "type": "Superpowered.PitchShift", "config": { "enabled": false, "pitchShiftCents": -400 } },
                          { "id": "h_lowGain", "type": "Gain" },
                          { "id": "h_highPitch", "type": "Superpowered.PitchShift", "config": { "enabled": false, "pitchShiftCents": 400 } },
                          { "id": "h_highGain", "type": "Gain" },
                          { "id": "h_mixer", "type": "Mixer" }
                        ],
                        "connections": [
                          { "sourceNode": "inputNode", "destinationNode": "h_avpc" },
                          { "sourceNode": "h_avpc", "destinationNode": "h_split" },
                          { "sourceNode": "h_split", "destinationNode": "h_lowPitch" },
                          { "sourceNode": "h_split", "destinationNode": "h_highPitch" },
                          { "sourceNode": "h_split", "destinationNode": "h_mixer" },
                          { "sourceNode": "h_lowPitch", "destinationNode": "h_lowGain" },
                          { "sourceNode": "h_lowGain", "destinationNode": "h_mixer" },
                          { "sourceNode": "h_highPitch", "destinationNode": "h_highGain" },
                          { "sourceNode": "h_highGain", "destinationNode": "h_mixer" },
                          { "sourceNode": "h_mixer", "destinationNode": "outputNode" }
                        ]
                      } }
                    },
                    { "id": "vibrato", "type": "AudioEffects.Vibrato", "config": { "enabled": false } },
                    { "id": "chorus", "type": "AudioEffects.Chorus", "config": { "enabled": false } },
                    { "id": "flanger", "type": "Superpowered.Flanger", "config": { "enabled": false } },
                    { "id": "delay", "type": "Superpowered.Echo", "config": { "enabled": false } },
                    { "id": "reverb", "type": "Superpowered.Reverb", "config": { "enabled": false } },
                    { "id": "mixer", "type": "Mixer" },
                    { "id": "outputMixer", "type": "Mixer" },
                    { "id": "vuMeter", "type": "VUMeter", "config": { "smoothingDurationMs": 100.0 } },
                    { "id": "inputSplitter", "type": "BusSplitter" },
                    { "id": "musicSplitter", "type": "BusSplitter" },
                    { "id": "volumeSplitter", "type": "BusSplitter" },
                    { "id": "micToMono", "type": "MultiChannelToMono" },
                    { "id": "channelSplitter", "type": "ChannelSplitter" },
                    { "id": "ivsSink", "type": "AmazonIVS.Sink" }
                  ],
                  "connections": [
                    { "sourceNode": "inputNode", "destinationNode": "inputSplitter" },
                    { "sourceNode": "inputSplitter", "destinationNode": "micToMono" },
                    { "sourceNode": "micToMono", "destinationNode": "vuMeter" },
                    { "sourceNode": "inputSplitter", "destinationNode": "voiceGain" },
                    { "sourceNode": "voiceGain", "destinationNode": "harmonizer" },
                    { "sourceNode": "harmonizer", "destinationNode": "vibrato" },
                    { "sourceNode": "vibrato", "destinationNode": "chorus" },
                    { "sourceNode": "chorus", "destinationNode": "flanger" },
                    { "sourceNode": "flanger", "destinationNode": "delay" },
                    { "sourceNode": "delay", "destinationNode": "reverb" },
                    { "sourceNode": "reverb", "destinationNode": "volumeSplitter" },
                    { "sourceNode": "volumeSplitter", "destinationNode": "mixer" },
                    { "sourceNode": "musicPlayer", "destinationNode": "musicGain" },
                    { "sourceNode": "musicGain", "destinationNode": "musicSplitter" },
                    { "sourceNode": "musicSplitter", "destinationNode": "mixer" },
                    { "sourceNode": "mixer", "destinationNode": "channelSplitter" },
                    { "sourceNode": "channelSplitter", "destinationNode": "ivsSink" },
                    { "sourceNode": "channelSplitter", "destinationNode": "recorder" },
                    { "sourceNode": "musicSplitter", "destinationNode": "outputMixer" },
                    { "sourceNode": "recordingPlayer", "destinationNode": "outputMixer" },
                    { "sourceNode": "volumeSplitter", "destinationNode": "volumeOutputGain" },
                    { "sourceNode": "volumeOutputGain", "destinationNode": "outputMixer" },
                    { "sourceNode": "outputMixer", "destinationNode": "outputNode" }
                  ]
                }
              }
            }
        """.trimIndent()
    }

    private var engineId: String = ""

    val mixedFilePath: String
        get() = File(File(sdkTempDir(), "output"), "recording.wav").absolutePath

    private var audioDevice: AudioDevice? = null
    private val deviceDiscovery = DeviceDiscovery(context)
    private val publishStreams = ArrayList<LocalStageStream>()
    private var stage: Stage? = null

    private val stageStrategy = object : Stage.Strategy {
        override fun stageStreamsToPublishForParticipant(stage: Stage, p: ParticipantInfo) = publishStreams
        override fun shouldPublishFromParticipant(stage: Stage, p: ParticipantInfo) = true

        @RequiresApi(Build.VERSION_CODES.P)
        override fun shouldSubscribeToParticipant(stage: Stage, p: ParticipantInfo) = Stage.SubscribeType.AUDIO_ONLY
    }

    init {
        createStage()
        audioDevice = deviceDiscovery.createAudioInputSource(
            1,
            BroadcastConfiguration.AudioSampleRate.RATE_48000,
            AudioDevice.Format.INT16,
        )
        audioDevice?.let {
            publishStreams.add(AudioLocalStageStream(it))
            AmazonIVSExtension.registerAudioBus(IVSInterface(it))
        }
    }

    fun startAudioGraph() {
        val result = Switchboard.createEngine(GRAPH_JSON)
        if (result.isError) throw RuntimeException("Failed to create audio engine: ${result.error}")
        engineId = result.value!!
        Switchboard.setValue(MUSIC_GAIN, "gain", 0.5f)
        Switchboard.setValue(VOLUME_OUTPUT_GAIN, "gain", 0.0f)
        Switchboard.callAction(engineId, "start")
    }

    fun stopAudioGraph() {
        if (engineId.isNotEmpty()) {
            Switchboard.callAction(engineId, "stop")
            Switchboard.destroyEngine(engineId)
            engineId = ""
        }
    }

    fun loadSong(assetName: String) =
        Switchboard.callAction(MUSIC_PLAYER, "load", mapOf("audioFilePath" to copyAssetToCache(assetName)))

    val isPlayingMusic: Boolean get() = readBoolean(MUSIC_PLAYER, "isPlaying")
    val isPlayingRecording: Boolean get() = readBoolean(RECORDING_PLAYER, "isPlaying")

    fun playMusic() = Switchboard.callAction(MUSIC_PLAYER, "play")
    fun pauseMusic() = Switchboard.callAction(MUSIC_PLAYER, "pause")

    fun playRecording() {
        Switchboard.callAction(RECORDING_PLAYER, "load", mapOf("audioFilePath" to mixedFilePath))
        Switchboard.callAction(RECORDING_PLAYER, "play")
    }

    fun stopRecordingPlayback() = Switchboard.callAction(RECORDING_PLAYER, "stop")

    fun setMusicVolume(volume: Int) = Switchboard.setValue(MUSIC_GAIN, "gain", volume / 100.0f)
    fun setVoiceVolume(volume: Int) = Switchboard.setValue(VOICE_GAIN, "gain", volume / 100.0f)
    fun setMonitorVoice(enabled: Boolean) = Switchboard.setValue(VOLUME_OUTPUT_GAIN, "gain", if (enabled) 1.0f else 0.0f)

    fun setVibratoEnabled(enabled: Boolean) = Switchboard.setValue(VIBRATO, "enabled", enabled)
    fun setChorusEnabled(enabled: Boolean) = Switchboard.setValue(CHORUS, "enabled", enabled)
    fun setFlangerEnabled(enabled: Boolean) = Switchboard.setValue(FLANGER, "enabled", enabled)
    fun setDelayEnabled(enabled: Boolean) = Switchboard.setValue(DELAY, "enabled", enabled)
    fun setReverbEnabled(enabled: Boolean) = Switchboard.setValue(REVERB, "enabled", enabled)

    fun getSongDurationInSeconds(): Double = readDouble(MUSIC_PLAYER, "duration")
    fun getPositionInSeconds(): Double = readDouble(MUSIC_PLAYER, "position")
    fun getProgress(): Float {
        val d = getSongDurationInSeconds()
        return if (d > 0.0) (getPositionInSeconds() / d).toFloat() else 0f
    }
    val vuPeak: Float get() = readDouble(VU_METER, "peak").toFloat()

    fun startStream() {
        if (isPlayingRecording) stopRecordingPlayback()
        Switchboard.callAction("recorder", "start")
        joinStage()
    }

    fun stopStream() {
        Switchboard.callAction("recorder", "stop")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) stage?.leave()
    }

    private fun createStage() {
        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN)
        if (token.isBlank()) {
            DialogHelper.create(context, "Please add your publisher token in settings!")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stage = Stage(context, token.trim(), stageStrategy)
        }
    }

    private fun joinStage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) stage?.join()
        } catch (e: BroadcastException) {
            Log.d(TAG, "joinStage error: ${e.message}")
        }
    }

    private fun sdkTempDir(): String {
        val result = Switchboard.getValue("switchboard", "tempDirPath")
        val path = if (result.isError) "" else (result.value as? String ?: "")
        return path.ifEmpty { context.cacheDir.absolutePath }
    }

    private fun readBoolean(id: String, key: String): Boolean {
        val result = Switchboard.getValue(id, key)
        return if (result.isError) false else (result.value as? Boolean ?: false)
    }

    private fun readDouble(id: String, key: String): Double {
        val result = Switchboard.getValue(id, key)
        return if (result.isError) 0.0 else (result.value as? Number)?.toDouble() ?: 0.0
    }

    private fun copyAssetToCache(assetName: String): String {
        val outFile = File(context.cacheDir, assetName)
        if (!outFile.exists()) {
            context.assets.open(assetName).use { input -> outFile.outputStream().use { input.copyTo(it) } }
        }
        return outFile.absolutePath
    }
}
