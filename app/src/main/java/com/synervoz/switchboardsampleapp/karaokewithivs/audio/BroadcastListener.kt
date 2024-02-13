package com.synervoz.switchboardsampleapp.karaokewithivs.audio

import android.util.Log
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.BroadcastSession
import com.amazonaws.ivs.broadcast.Device

class BroadcastListener: BroadcastSession.Listener() {

    companion object {
        val TAG = this::class.java.name
    }

    override fun onAnalyticsEvent(name: String, properties: String) {
        super.onAnalyticsEvent(name, properties)
        Log.d(TAG, "Analytics $name - $properties")
    }

    override fun onStateChanged(state: BroadcastSession.State) {
        when (state) {
            BroadcastSession.State.CONNECTED -> {
                Log.d(TAG, "Connected state")
            }

            BroadcastSession.State.DISCONNECTED -> {
                Log.d(TAG, "Disconnected state")
            }

            BroadcastSession.State.CONNECTING -> {
                Log.d(TAG, "Connecting state")
            }

            BroadcastSession.State.ERROR -> {
                Log.d(TAG, "Error state")
            }

            BroadcastSession.State.INVALID -> {
                Log.d(TAG, "Invalid state")
            }
        }
    }

    override fun onAudioStats(peak: Double, rms: Double) {
        super.onAudioStats(peak, rms)
        Log.d(TAG, "Audio stats received - peak ($peak), rms ($rms)")
    }

    override fun onDeviceRemoved(descriptor: Device.Descriptor) {
        super.onDeviceRemoved(descriptor)
        Log.d(TAG, "Device removed: ${descriptor.deviceId} - ${descriptor.type}")
    }

    override fun onDeviceAdded(descriptor: Device.Descriptor) {
        super.onDeviceAdded(descriptor)
        Log.d(TAG, "Device added: ${descriptor.urn} - ${descriptor.friendlyName} - ${descriptor.deviceId} - ${descriptor.position}")
    }

    override fun onError(error: BroadcastException) {
        Log.d(TAG, "Error is: ${error.detail} Error code: ${error.code} Error source: ${error.source}")
    }
}