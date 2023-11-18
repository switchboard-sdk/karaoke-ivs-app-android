package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.BroadcastSession
import com.amazonaws.ivs.broadcast.DeviceDiscovery
import com.amazonaws.ivs.broadcast.LocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
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
import com.synervoz.switchboard.sdk.audiographnodes.SubgraphProcessorNode
import com.synervoz.switchboard.sdk.audiographnodes.VUMeterNode
import com.synervoz.switchboardamazonivs.audiographnodes.IVSBroadcastSinkNode
import com.synervoz.switchboardaudioeffects.audiographnodes.ChorusNode
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import com.synervoz.switchboardsuperpowered.audiographnodes.EchoNode
import com.synervoz.switchboardsuperpowered.audiographnodes.FlangerNode
import com.synervoz.switchboardsuperpowered.audiographnodes.ReverbNode
import com.synervoz.switchboardsampleapp.karaokewithivs.config.token
//import com.synervoz.switchboardvoicemod.audiographnodes.VoicemodNode
//
class KaraokeWithIVSRealtimeExample(val context: Context) {

    companion object {
        val TAG = this::class.java.name
    }

    val audioGraph = AudioGraph()
    val audioEngine = AudioEngine(
        context = context, microphoneEnabled = true, performanceMode = PerformanceMode.NONE,
        micInputPreset = MicInputPreset.GENERIC
    )
    val audioPlayerNode = AudioPlayerNode()
    val busSplitterNode = BusSplitterNode()
    val channelSplitterNode = ChannelSplitterNode()
    val ivsSinkNode: IVSBroadcastSinkNode
    val flangerNode = FlangerNode()
    val chorusNode = ChorusNode()
    val reverbNode = ReverbNode()
    val delayNode = EchoNode()
    val mixerNode = MixerNode()
    val vuMeterNode = VUMeterNode()
    val splitterNode = BusSplitterNode()
    val multiChannelToMonoNode = MultiChannelToMonoNode()
    val harmonizer = HarmonizerEffect()
    val harmonizerChainNode = SubgraphProcessorNode()
//    val voicemodNode = VoicemodNode()

    val musicGainNode = GainNode()
    val voiceGainNode = GainNode()

    var session: BroadcastSession? = null
    var audioDevice: AudioDevice? = null
    var deviceDiscovery = DeviceDiscovery(context)
    var publishStreams: ArrayList<LocalStageStream> = ArrayList()

    private var stage: Stage? = null

    private val stageStrategy = object : Stage.Strategy {
        override fun stageStreamsToPublishForParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): List<LocalStageStream> {
            return publishStreams
        }

        override fun shouldPublishFromParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): Boolean {
            return true
        }

        @RequiresApi(Build.VERSION_CODES.P)
        override fun shouldSubscribeToParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): Stage.SubscribeType {
            return Stage.SubscribeType.AUDIO_ONLY
        }
    }

    init {
        audioPlayerNode.isLoopingEnabled = true
        vuMeterNode.smoothingDurationMs = 100.0f

        createStage()

        val sampleRate = when (audioEngine.sampleRate) {
            8000 -> BroadcastConfiguration.AudioSampleRate.RATE_8000
            16000 -> BroadcastConfiguration.AudioSampleRate.RATE_16000
            22050 -> BroadcastConfiguration.AudioSampleRate.RATE_22050
            44100 -> BroadcastConfiguration.AudioSampleRate.RATE_44100
            48000 -> BroadcastConfiguration.AudioSampleRate.RATE_48000
            else -> BroadcastConfiguration.AudioSampleRate.RATE_44100
        }

        audioDevice =
            deviceDiscovery.createAudioInputSource(1, sampleRate, AudioDevice.Format.INT16)
        val microphoneStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AudioLocalStageStream(audioDevice!!)
        } else {
            TODO("VERSION.SDK_INT < P")
        }
        publishStreams.add(microphoneStream)

        ivsSinkNode = IVSBroadcastSinkNode(audioDevice!!)

        audioGraph.addNode(musicGainNode)
        audioGraph.addNode(voiceGainNode)
        audioGraph.addNode(audioPlayerNode)
        audioGraph.addNode(busSplitterNode)
        audioGraph.addNode(channelSplitterNode)
        audioGraph.addNode(ivsSinkNode)
        audioGraph.addNode(flangerNode)
        audioGraph.addNode(chorusNode)
        audioGraph.addNode(delayNode)
        audioGraph.addNode(reverbNode)
        audioGraph.addNode(mixerNode)
        audioGraph.addNode(vuMeterNode)
        audioGraph.addNode(splitterNode)
        audioGraph.addNode(multiChannelToMonoNode)
        audioGraph.addNode(harmonizerChainNode)
//        audioGraph.addNode(voicemodNode)

        flangerNode.isEnabled = false
        delayNode.isEnabled = false
        reverbNode.isEnabled = false
        musicGainNode.gain = 0.5f
        delayNode.beats = 1f

        audioGraph.connect(audioGraph.inputNode, splitterNode)
        audioGraph.connect(splitterNode, multiChannelToMonoNode)
        audioGraph.connect(multiChannelToMonoNode, vuMeterNode)
        audioGraph.connect(splitterNode, voiceGainNode)
        audioGraph.connect(voiceGainNode, harmonizerChainNode)
        audioGraph.connect(harmonizerChainNode, chorusNode) //harmonizer + audiotuner
//        audioGraph.connect(voiceGainNode, flangerNode)
//        audioGraph.connect(voicemodNode, flangerNode)
        audioGraph.connect(chorusNode, flangerNode)
        audioGraph.connect(flangerNode, delayNode)
        audioGraph.connect(delayNode, reverbNode)
        audioGraph.connect(reverbNode, mixerNode)

        audioGraph.connect(audioPlayerNode, musicGainNode)
        audioGraph.connect(musicGainNode, busSplitterNode)
        audioGraph.connect(busSplitterNode, mixerNode)
        audioGraph.connect(mixerNode, channelSplitterNode)
        audioGraph.connect(channelSplitterNode, ivsSinkNode)

        audioGraph.connect(busSplitterNode, audioGraph.outputNode)
        harmonizerChainNode.setAudioGraph(harmonizer.audioGraph)
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
//        audioPlayerNode.play()
        joinStage()
    }

    fun stopStream() {
        audioPlayerNode.pause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stage?.leave()
        }
        session?.stop()
    }

    fun setMusicVolume(volume: Int) {
        musicGainNode.gain = volume / 100.0f
    }

    fun setVoiceVolume(volume: Int) {
        voiceGainNode.gain = volume / 100.0f
    }

    fun getSongDurationInSeconds(): Double {
        return audioPlayerNode.getDuration()
    }

    fun getPositionInSeconds(): Double {
        return audioPlayerNode.position
    }

    fun setPositionInSeconds(position: Double) {
        audioPlayerNode.position = position
    }

    fun getProgress(): Float {
        return (audioPlayerNode.position / audioPlayerNode.getDuration()).toFloat()
    }

    private fun createStage() {
        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN) ?: token
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Stage(
                context,
                token.trim(),
                stageStrategy
            ).apply {
                stage = this
            }
        }
    }

    private fun joinStage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                stage?.join()
            }
        } catch (exception: BroadcastException) {
            Log.d(TAG, "createStage: error " + exception.message)
        }
    }

}