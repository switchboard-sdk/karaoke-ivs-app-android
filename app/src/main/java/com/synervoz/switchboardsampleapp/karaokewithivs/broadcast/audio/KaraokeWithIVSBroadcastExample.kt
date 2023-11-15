package com.synervoz.switchboardsampleapp.karaokewithivs.broadcast.audio

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.BroadcastSession
import com.amazonaws.ivs.broadcast.Device
import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audioengine.MicInputPreset
import com.synervoz.switchboard.sdk.audioengine.PerformanceMode
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.AudioPlayerNode
import com.synervoz.switchboard.sdk.audiographnodes.BusSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.ChannelSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.GainNode
import com.synervoz.switchboard.sdk.audiographnodes.MixerNode
import com.synervoz.switchboard.sdk.audiographnodes.MultiChannelToMonoNode
import com.synervoz.switchboard.sdk.audiographnodes.VUMeterNode
import com.synervoz.switchboardamazonivs.audiographnodes.IVSBroadcastSinkNode
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import com.synervoz.switchboardsampleapp.karaokewithivs.audio.BroadcastListener
import com.synervoz.switchboardsampleapp.karaokewithivs.config.ingestServer
import com.synervoz.switchboardsampleapp.karaokewithivs.config.streamKey
import com.synervoz.switchboardsuperpowered.audiographnodes.EchoNode
import com.synervoz.switchboardsuperpowered.audiographnodes.FlangerNode
import com.synervoz.switchboardsuperpowered.audiographnodes.ReverbNode


class KaraokeWithIVSBroadcastExample(val context: Context) {

    companion object {
        val TAG = this::class.java.name
    }

    val audioGraph = AudioGraph()
    val audioEngine = AudioEngine(context = context, microphoneEnabled = true, performanceMode = PerformanceMode.NONE,
        micInputPreset = MicInputPreset.GENERIC)
    val audioPlayerNode = AudioPlayerNode()
    val busSplitterNode = BusSplitterNode()
    val channelSplitterNode = ChannelSplitterNode()
    val ivsSinkNode: IVSBroadcastSinkNode
    val flangerNode = FlangerNode()
    val reverbNode = ReverbNode()
    val delayNode = EchoNode()
    val mixerNode = MixerNode()
    val vuMeterNode = VUMeterNode()
    val splitterNode = BusSplitterNode()
    val multiChannelToMonoNode = MultiChannelToMonoNode()

    val musicGainNode = GainNode()
    val voiceGainNode = GainNode()

    var session: BroadcastSession? = null
    var audioDevice: AudioDevice? = null

    private val broadcastListener = BroadcastListener()

    init {
        audioPlayerNode.isLoopingEnabled = true
        vuMeterNode.smoothingDurationMs = 100.0f

        createSession()

        val sampleRate = when (audioEngine.sampleRate) {
            8000 -> BroadcastConfiguration.AudioSampleRate.RATE_8000
            16000 -> BroadcastConfiguration.AudioSampleRate.RATE_16000
            22050 -> BroadcastConfiguration.AudioSampleRate.RATE_22050
            44100 -> BroadcastConfiguration.AudioSampleRate.RATE_44100
            48000 -> BroadcastConfiguration.AudioSampleRate.RATE_48000
            else -> BroadcastConfiguration.AudioSampleRate.RATE_44100
        }

        audioDevice = session?.createAudioInputSource(1, sampleRate, AudioDevice.Format.INT16)

        ivsSinkNode = IVSBroadcastSinkNode(audioDevice!!)

        audioGraph.addNode(musicGainNode)
        audioGraph.addNode(voiceGainNode)
        audioGraph.addNode(audioPlayerNode)
        audioGraph.addNode(busSplitterNode)
        audioGraph.addNode(channelSplitterNode)
        audioGraph.addNode(mixerNode)
        audioGraph.addNode(ivsSinkNode)
        audioGraph.addNode(flangerNode)
        audioGraph.addNode(delayNode)
        audioGraph.addNode(reverbNode)
        audioGraph.addNode(vuMeterNode)
        audioGraph.addNode(splitterNode)
        audioGraph.addNode(multiChannelToMonoNode)

        flangerNode.isEnabled = false
        delayNode.isEnabled = false
        reverbNode.isEnabled = false
        musicGainNode.gain = 0.5f

        audioGraph.connect(audioGraph.inputNode, splitterNode)
        audioGraph.connect(splitterNode, multiChannelToMonoNode)
        audioGraph.connect(multiChannelToMonoNode, vuMeterNode)
        audioGraph.connect(splitterNode, voiceGainNode)
        audioGraph.connect(voiceGainNode, flangerNode)
        audioGraph.connect(flangerNode, delayNode)
        audioGraph.connect(delayNode, reverbNode)
        audioGraph.connect(reverbNode, mixerNode)

        audioGraph.connect(audioPlayerNode, musicGainNode)
        audioGraph.connect(musicGainNode, busSplitterNode)
        audioGraph.connect(busSplitterNode, mixerNode)
        audioGraph.connect(mixerNode, channelSplitterNode)
        audioGraph.connect(channelSplitterNode, ivsSinkNode)

        audioGraph.connect(busSplitterNode, audioGraph.outputNode)
    }

    fun isPlaying() = audioPlayerNode.isPlaying

    fun play() {
        audioPlayerNode.play()
    }

    fun pause() {
        audioPlayerNode.pause()
    }


    fun startAudioGraph() {
        audioEngine.start(audioGraph)
    }

    fun stopAudioGraph() {
        audioEngine.stop()
        audioGraph.close()
        channelSplitterNode.close()
        ivsSinkNode.close()
    }

    fun startStream() {
        val ingestServer = PreferenceManager.getGlobalStringPreference(PreferenceConstants.INGEST_SERVER) ?: ingestServer
        val streamKey = PreferenceManager.getGlobalStringPreference(PreferenceConstants.STREAM_KEY) ?: streamKey
        session?.start(ingestServer, streamKey)
    }

    fun stopStream() {
        audioPlayerNode.pause()
        session?.stop()
    }

    fun setMusicVolume(volume: Int) {
        musicGainNode.gain = volume / 100.0f
    }

    fun setVoiceVolume(volume: Int) {
        voiceGainNode.gain = volume / 100.0f
    }

    fun getSongDurationInSeconds() : Double {
        return audioPlayerNode.getDuration()
    }

    fun getPositionInSeconds() : Double {
        return audioPlayerNode.position
    }

    fun setPositionInSeconds(position: Double) {
        audioPlayerNode.position = position
    }

    fun getProgress(): Float {
        return (audioPlayerNode.position / audioPlayerNode.getDuration()).toFloat()
    }

    fun createSession(onReady: () -> Unit = {}) {
        session?.release()

        val config = BroadcastConfiguration().apply {
            // This slot will hold the custom audio.
            val slot = BroadcastConfiguration.Mixer.Slot.with {
                it.preferredAudioInput = Device.Descriptor.DeviceType.USER_AUDIO
                return@with it
            }
            this.mixer.slots = arrayOf(slot)
        }

        BroadcastSession(context, broadcastListener, config, null).apply {
            session = this
            Log.d(TAG, "Broadcast session ready: $isReady")
            if (isReady) {
                onReady()
            } else {
                Log.d(TAG, "Broadcast session not ready")
                Toast.makeText(context, "Failed to create Session", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

}