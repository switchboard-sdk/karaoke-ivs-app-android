package com.synervoz.switchboardsampleapp.karaokewithivs.broadcast.audio

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.BroadcastSession
import com.amazonaws.ivs.broadcast.Device
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardamazonivs.AmazonIVSExtension
import com.synervoz.switchboardamazonivs.audioInterface.IVSInterface
import com.synervoz.switchboardsampleapp.karaokewithivs.audio.BroadcastListener
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.DialogHelper
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import java.io.File

/**
 * Karaoke broadcast example on the SwitchboardSDK v3 JSON graph API.
 *
 * The microphone is metered and, through a Flanger/Echo/Reverb chain, mixed with a looping backing
 * track; the mix is downmixed to mono and sent to Amazon IVS via an `AmazonIVS.Sink` node. The
 * backing track alone is monitored on the speaker. The IVS audio device (created from the
 * [BroadcastSession]) is registered as the shared IVS audio bus so the JSON-created sink binds to
 * it (SWI-6647).
 *
 * ```
 * inputNode --> inputSplitter --> micToMono --> vuMeter
 *                            \--> voiceGain --> flanger --> delay --> reverb --> mixer
 * musicPlayer --> musicGain --> musicSplitter --> mixer / outputNode (monitor)
 * mixer --> channelSplitter --> ivsSink (AmazonIVS.Sink)
 * ```
 */
class KaraokeWithIVSBroadcastExample(val context: Context) {

    companion object {
        val TAG: String = this::class.java.name
        private const val SAMPLE_RATE = 48000

        private const val MUSIC_PLAYER = "musicPlayer"
        private const val MUSIC_GAIN = "musicGain"
        private const val VOICE_GAIN = "voiceGain"
        private const val FLANGER = "flanger"
        private const val DELAY = "delay"
        private const val REVERB = "reverb"
        private const val VU_METER = "vuMeter"

        private val GRAPH_JSON = """
            {
              "type": "Realtime",
              "config": {
                "microphoneEnabled": true,
                "sampleRate": $SAMPLE_RATE,
                "graph": {
                  "nodes": [
                    { "id": "musicPlayer", "type": "AudioPlayer", "config": { "isLoopingEnabled": true } },
                    { "id": "musicGain", "type": "Gain" },
                    { "id": "voiceGain", "type": "Gain" },
                    { "id": "flanger", "type": "Superpowered.Flanger", "config": { "enabled": false } },
                    { "id": "delay", "type": "Superpowered.Echo", "config": { "enabled": false } },
                    { "id": "reverb", "type": "Superpowered.Reverb", "config": { "enabled": false } },
                    { "id": "mixer", "type": "Mixer" },
                    { "id": "channelSplitter", "type": "ChannelSplitter" },
                    { "id": "ivsSink", "type": "AmazonIVS.Sink" },
                    { "id": "vuMeter", "type": "VUMeter", "config": { "smoothingDurationMs": 100.0 } },
                    { "id": "inputSplitter", "type": "BusSplitter" },
                    { "id": "micToMono", "type": "MultiChannelToMono" },
                    { "id": "musicSplitter", "type": "BusSplitter" }
                  ],
                  "connections": [
                    { "sourceNode": "inputNode", "destinationNode": "inputSplitter" },
                    { "sourceNode": "inputSplitter", "destinationNode": "micToMono" },
                    { "sourceNode": "micToMono", "destinationNode": "vuMeter" },
                    { "sourceNode": "inputSplitter", "destinationNode": "voiceGain" },
                    { "sourceNode": "voiceGain", "destinationNode": "flanger" },
                    { "sourceNode": "flanger", "destinationNode": "delay" },
                    { "sourceNode": "delay", "destinationNode": "reverb" },
                    { "sourceNode": "reverb", "destinationNode": "mixer" },
                    { "sourceNode": "musicPlayer", "destinationNode": "musicGain" },
                    { "sourceNode": "musicGain", "destinationNode": "musicSplitter" },
                    { "sourceNode": "musicSplitter", "destinationNode": "mixer" },
                    { "sourceNode": "musicSplitter", "destinationNode": "outputNode" },
                    { "sourceNode": "mixer", "destinationNode": "channelSplitter" },
                    { "sourceNode": "channelSplitter", "destinationNode": "ivsSink" }
                  ]
                }
              }
            }
        """.trimIndent()
    }

    private var engineId: String = ""
    private val broadcastListener = BroadcastListener()

    var session: BroadcastSession? = null
    var audioDevice: AudioDevice? = null

    init {
        createSession()
        audioDevice = session?.createAudioInputSource(
            1,
            BroadcastConfiguration.AudioSampleRate.RATE_48000,
            AudioDevice.Format.INT16,
        )
        // Register the IVS device as the shared audio bus so the JSON AmazonIVS.Sink binds to it.
        audioDevice?.let { AmazonIVSExtension.registerAudioBus(IVSInterface(it)) }
    }

    fun startAudioGraph() {
        val result = Switchboard.createEngine(GRAPH_JSON)
        if (result.isError) throw RuntimeException("Failed to create audio engine: ${result.error}")
        engineId = result.value!!
        Switchboard.setValue(MUSIC_GAIN, "gain", 0.5f)
        Switchboard.callAction(engineId, "start")
    }

    fun stopAudioGraph() {
        if (engineId.isNotEmpty()) {
            Switchboard.callAction(engineId, "stop")
            Switchboard.destroyEngine(engineId)
            engineId = ""
        }
    }

    fun loadSong(assetName: String) {
        Switchboard.callAction(MUSIC_PLAYER, "load", mapOf("audioFilePath" to copyAssetToCache(assetName)))
    }

    val isPlaying: Boolean
        get() = readBoolean(MUSIC_PLAYER, "isPlaying")

    fun play() = Switchboard.callAction(MUSIC_PLAYER, "play")

    fun pause() = Switchboard.callAction(MUSIC_PLAYER, "pause")

    fun setMusicVolume(volume: Int) = Switchboard.setValue(MUSIC_GAIN, "gain", volume / 100.0f)

    fun setVoiceVolume(volume: Int) = Switchboard.setValue(VOICE_GAIN, "gain", volume / 100.0f)

    fun setFlangerEnabled(enabled: Boolean) = Switchboard.setValue(FLANGER, "enabled", enabled)

    fun setDelayEnabled(enabled: Boolean) = Switchboard.setValue(DELAY, "enabled", enabled)

    fun setReverbEnabled(enabled: Boolean) = Switchboard.setValue(REVERB, "enabled", enabled)

    fun getSongDurationInSeconds(): Double = readDouble(MUSIC_PLAYER, "duration")

    fun getPositionInSeconds(): Double = readDouble(MUSIC_PLAYER, "position")

    fun setPositionInSeconds(position: Double) = Switchboard.setValue(MUSIC_PLAYER, "position", position).let {}

    fun getProgress(): Float {
        val d = getSongDurationInSeconds()
        return if (d > 0.0) (getPositionInSeconds() / d).toFloat() else 0f
    }

    val vuPeak: Float
        get() = readDouble(VU_METER, "peak").toFloat()

    fun startStream() {
        val ingestServer = PreferenceManager.getGlobalStringPreference(PreferenceConstants.INGEST_SERVER)
        val streamKey = PreferenceManager.getGlobalStringPreference(PreferenceConstants.STREAM_KEY)
        if (ingestServer.isBlank() || streamKey.isBlank()) {
            DialogHelper.create(context, "Please add your ingest server and stream key in settings!")
        } else {
            session?.start(ingestServer, streamKey)
        }
    }

    fun stopStream() {
        pause()
        session?.stop()
    }

    private fun createSession(onReady: () -> Unit = {}) {
        session?.release()
        val config = BroadcastConfiguration().apply {
            val slot = BroadcastConfiguration.Mixer.Slot.with {
                it.preferredAudioInput = Device.Descriptor.DeviceType.USER_AUDIO
                return@with it
            }
            this.mixer.slots = arrayOf(slot)
        }
        BroadcastSession(context, broadcastListener, config, null).apply {
            session = this
            Log.d(TAG, "Broadcast session ready: $isReady")
            if (isReady) onReady() else Toast.makeText(context, "Failed to create Session", Toast.LENGTH_SHORT).show()
        }
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
