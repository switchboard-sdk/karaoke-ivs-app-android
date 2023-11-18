package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio

import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.BusSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.GainNode
import com.synervoz.switchboard.sdk.audiographnodes.MixerNode
import com.synervoz.switchboardsuperpowered.audiographnodes.AutomaticVocalPitchCorrectionNode
import com.synervoz.switchboardsuperpowered.audiographnodes.PitchShiftNode

interface FXChain {
    val audioGraph: AudioGraph
    var autoTunerisEnabled: Boolean
    var harmonizerIsEnabled: Boolean
}

/**
 * Container Auto tune and harmonizer effect
 */
class HarmonizerEffect : FXChain {
    private val avpcNode = AutomaticVocalPitchCorrectionNode()
    private val busSplitterNode = BusSplitterNode()
    private val lowPitchShiftNode = PitchShiftNode()
    private val lowPitchShiftGainNode = GainNode()
    private val highPitchShiftNode = PitchShiftNode()
    private val highPitchShiftGainNode = GainNode()
    private val mixerNode = MixerNode()

    override val audioGraph = AudioGraph()

    override var autoTunerisEnabled = avpcNode.isEnabled
        get() {
            return avpcNode.isEnabled
        }
        set(value) {
            avpcNode.isEnabled = value
            field = value
        }

    override var harmonizerIsEnabled = lowPitchShiftNode.isEnabled
        get() {
            return lowPitchShiftNode.isEnabled
        }
        set(value) {
            lowPitchShiftNode.isEnabled = value
            highPitchShiftNode.isEnabled = value
            field = value
        }

    fun setHighPreset() {
        avpcNode.isEnabled = false
        avpcNode.speed = AutomaticVocalPitchCorrectionNode.TunerSpeed.EXTREME
        avpcNode.range = AutomaticVocalPitchCorrectionNode.TunerRange.WIDE
        avpcNode.scale = AutomaticVocalPitchCorrectionNode.TunerScale.FMAJOR

        lowPitchShiftNode.isEnabled = false
        lowPitchShiftNode.pitchShiftCents = -400
        lowPitchShiftGainNode.gain = 0.8f

        highPitchShiftNode.isEnabled = false
        highPitchShiftNode.pitchShiftCents = 400
        highPitchShiftGainNode.gain = 1.0f
    }

    init {
        setHighPreset()
        audioGraph.addNode(avpcNode)
        audioGraph.addNode(busSplitterNode)
        audioGraph.addNode(lowPitchShiftNode)
        audioGraph.addNode(lowPitchShiftGainNode)
        audioGraph.addNode(highPitchShiftNode)
        audioGraph.addNode(highPitchShiftGainNode)
        audioGraph.addNode(mixerNode)

        audioGraph.connect(audioGraph.inputNode, avpcNode)
        audioGraph.connect(avpcNode, busSplitterNode)
        audioGraph.connect(busSplitterNode, lowPitchShiftNode)
        audioGraph.connect(busSplitterNode, highPitchShiftNode)
        audioGraph.connect(busSplitterNode, mixerNode)
        audioGraph.connect(lowPitchShiftNode, lowPitchShiftGainNode)
        audioGraph.connect(lowPitchShiftGainNode, mixerNode)
        audioGraph.connect(highPitchShiftNode, highPitchShiftGainNode)
        audioGraph.connect(highPitchShiftGainNode, mixerNode)
        audioGraph.connect(mixerNode, audioGraph.outputNode)

        audioGraph.start()
    }
}