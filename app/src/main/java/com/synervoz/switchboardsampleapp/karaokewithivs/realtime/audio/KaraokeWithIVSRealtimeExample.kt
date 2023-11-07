package com.synervoz.switchboardsampleapp.karaokewithivs.broadcast.audio

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
import com.synervoz.switchboard.sdk.audiographnodes.VUMeterNode
import com.synervoz.switchboardamazonivs.audiographnodes.IVSBroadcastSinkNode
import com.synervoz.switchboardsampleapp.karaokewithivs.config.endpoint
import com.synervoz.switchboardsampleapp.karaokewithivs.config.streamKey
import com.synervoz.switchboardsuperpowered.audiographnodes.EchoNode
import com.synervoz.switchboardsuperpowered.audiographnodes.FlangerNode
import com.synervoz.switchboardsuperpowered.audiographnodes.ReverbNode


class KaraokeWithIVSRealtimeExample(val context: Context) {

    companion object {
        val TAG = this::class.java.name
    }

    val audioGraph = AudioGraph()
    val audioEngine = AudioEngine(
        context = context, microphoneEnabled = true, performanceMode = PerformanceMode.LOW_LATENCY,
        micInputPreset = MicInputPreset.VoicePerformance
    )
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
        audioGraph.addNode(mixerNode)
        audioGraph.addNode(ivsSinkNode)
        audioGraph.addNode(flangerNode)
        audioGraph.addNode(delayNode)
        audioGraph.addNode(reverbNode)
        audioGraph.addNode(mixerNode)
        audioGraph.addNode(vuMeterNode)
        audioGraph.addNode(splitterNode)
        audioGraph.addNode(multiChannelToMonoNode)

        flangerNode.isEnabled = false
        delayNode.isEnabled = false
        reverbNode.isEnabled = false

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
        musicGainNode.close()
        voiceGainNode.close()
        audioPlayerNode.close()
        busSplitterNode.close()
        mixerNode.close()
        ivsSinkNode.close()
        flangerNode.close()
        delayNode.close()
        reverbNode.close()
        mixerNode.close()
        vuMeterNode.close()
        splitterNode.close()
        multiChannelToMonoNode.close()
    }

    fun startStream() {
        audioPlayerNode.play()
        session?.start(endpoint, streamKey)
        joinStage()
    }

    fun stopStream() {
        audioPlayerNode.pause()
        stage?.leave()
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
        Stage(
            context,
            "eyJhbGciOiJLTVMiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE2OTkzNTM1NjQsImlhdCI6MTY5OTMxMDM2NCwianRpIjoiWXduMFB5NU92cjdNIiwicmVzb3VyY2UiOiJhcm46YXdzOml2czp1cy13ZXN0LTI6MTQ1NzIzNjg2MjQ2OnN0YWdlL1RrV1dzNXJOTktOWCIsInRvcGljIjoiVGtXV3M1ck5OS05YIiwiZXZlbnRzX3VybCI6IndzczovL2dsb2JhbC5lZXZlZS5ldmVudHMubGl2ZS12aWRlby5uZXQiLCJ3aGlwX3VybCI6Imh0dHBzOi8vYzg3MDU2N2QwOWQ3Lmdsb2JhbC1ibS53aGlwLmxpdmUtdmlkZW8ubmV0IiwidXNlcl9pZCI6InRqLTEiLCJjYXBhYmlsaXRpZXMiOnsiYWxsb3dfcHVibGlzaCI6dHJ1ZSwiYWxsb3dfc3Vic2NyaWJlIjp0cnVlfSwidmVyc2lvbiI6IjAuMCJ9.MGUCMEXuERQy8IdidVOUZkVD6F0J5pE3ie_CLb49BpDD3LfDk4u6QEIvSSmpWQocJ-VpmAIxAMaTS_Su_mwGIP-RGJRxT7fgiaDPtNRdz_k8VZQurq-hEaUcGJ5Cz-NnP0SDnuEJhQ",
            stageStrategy
        ).apply {
            stage = this
        }
    }

    private fun joinStage() {
        try {
            stage?.join()
        } catch (exception: BroadcastException) {
            Log.d(TAG, "createStage: error " + exception.message)
        }
    }

}