package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio

import android.content.Context
import android.os.Build
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.DeviceDiscovery
import com.amazonaws.ivs.broadcast.LocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audioengine.MicInputPreset
import com.synervoz.switchboard.sdk.audioengine.PerformanceMode
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.AudioPlayerNode
import com.synervoz.switchboard.sdk.audiographnodes.BusSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.MixerNode
import com.synervoz.switchboard.sdk.utils.AssetLoader
import com.synervoz.switchboardamazonivs.audiographnodes.IVSBroadcastSinkNode
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.DialogHelper
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager
import com.synervoz.switchboardsuperpowered.audiographnodes.AutomaticVocalPitchCorrectionNode
import com.synervoz.switchboardsuperpowered.audiographnodes.FlangerNode
import com.synervoz.switchboardsuperpowered.audiographnodes.ReverbNode

class KaraokeAppAudioEngine(val context: Context) {

    val audioGraph = AudioGraph()
    val audioPlayerNode = AudioPlayerNode()
    val mixerNode = MixerNode()
    val ivsSinkNode: IVSBroadcastSinkNode
    val autotuneNode = AutomaticVocalPitchCorrectionNode()
    val reverbNode = ReverbNode()
    val flangerNode = FlangerNode()
    val busSplitterNode = BusSplitterNode()

    var audioDevice: AudioDevice? = null

    val audioEngine = AudioEngine(
        context = context,
        microphoneEnabled = true,
        performanceMode = PerformanceMode.LOW_LATENCY,
        micInputPreset = MicInputPreset.VoicePerformance
    )

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

        override fun shouldSubscribeToParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): Stage.SubscribeType {
            return Stage.SubscribeType.NONE
        }
    }

    init {
        createStage()

        val sampleRate = when (audioEngine.sampleRate) {
            8000 -> BroadcastConfiguration.AudioSampleRate.RATE_8000
            16000 -> BroadcastConfiguration.AudioSampleRate.RATE_16000
            22050 -> BroadcastConfiguration.AudioSampleRate.RATE_22050
            44100 -> BroadcastConfiguration.AudioSampleRate.RATE_44100
            48000 -> BroadcastConfiguration.AudioSampleRate.RATE_48000
            else -> BroadcastConfiguration.AudioSampleRate.RATE_44100
        }

        audioDevice = deviceDiscovery.createAudioInputSource(1, sampleRate, AudioDevice.Format.INT16)
        val audioLocalStageStream = AudioLocalStageStream(audioDevice!!)

        publishStreams.add(audioLocalStageStream)
        ivsSinkNode = IVSBroadcastSinkNode(audioDevice!!)

        audioGraph.addNode(audioPlayerNode)
        audioGraph.addNode(mixerNode)
        audioGraph.addNode(ivsSinkNode)
        audioGraph.addNode(reverbNode)
        audioGraph.addNode(flangerNode)
        audioGraph.addNode(autotuneNode)
        audioGraph.addNode(busSplitterNode)

        audioGraph.connect(audioGraph.inputNode, autotuneNode)
        audioGraph.connect(autotuneNode, reverbNode)
        audioGraph.connect(reverbNode, flangerNode)
        audioGraph.connect(flangerNode, mixerNode)

        audioGraph.connect(audioPlayerNode, busSplitterNode)
        audioGraph.connect(busSplitterNode, mixerNode)
        audioGraph.connect(mixerNode, ivsSinkNode)
        audioGraph.connect(busSplitterNode, audioGraph.outputNode)
    }

    fun startAudioEngine() {
        audioEngine.start(audioGraph)
    }

    fun stopAudioEngine() {
        audioEngine.stop()
    }

    fun play() {
        audioPlayerNode.play()
    }

    fun pause() {
        audioPlayerNode.pause()
    }

    fun isPlayingMusic() = audioPlayerNode.isPlaying

    fun loadAudioFile(context: Context, assetName: String) {
        audioPlayerNode.load(
            AssetLoader.load(
                context, assetName
            ), Codec.createFromFileName(assetName)
        )
    }

    fun startStream() {
        stage?.join()
    }

    fun stopStream() {
        stage?.leave()
    }

    private fun createStage() {
        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN)

        if (token.isBlank()) {
            DialogHelper.create(context, "Please add your publisher token in settings!")
        } else {
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
    }
}