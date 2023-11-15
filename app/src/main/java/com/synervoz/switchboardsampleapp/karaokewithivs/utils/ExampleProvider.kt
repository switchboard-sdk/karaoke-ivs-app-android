package com.synervoz.switchboardsampleapp.karaokewithivs.utils

import android.content.Context
import androidx.fragment.app.Fragment
import com.synervoz.switchboard.sdk.SwitchboardSDK
import com.synervoz.switchboardamazonivs.AmazonIVSExtension
import com.synervoz.switchboardsampleapp.karaokewithivs.settings.IVSSettings
import com.synervoz.switchboardsampleapp.karaokewithivs.broadcast.fragment.KaraokeWithIVSBroadcastFragment
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.fragment.KaraokeWithIVSRealtimeFragment
import com.synervoz.switchboardsampleapp.karaokewithivs.config.superpoweredLicenseKey
import com.synervoz.switchboardsampleapp.karaokewithivs.config.switchboardClientID
import com.synervoz.switchboardsampleapp.karaokewithivs.config.switchboardClientSecret
import com.synervoz.switchboardsampleapp.karaokewithivs.config.voicemodClientKey
import com.synervoz.switchboardsuperpowered.SuperpoweredExtension
import com.synervoz.switchboardvoicemod.VoicemodExtension

object ExampleProvider {
    fun initialize(context: Context) {
        SwitchboardSDK.initialize(switchboardClientID, switchboardClientSecret)
        SuperpoweredExtension.initialize(superpoweredLicenseKey)
        VoicemodExtension.initialize(context, voicemodClientKey)
        AmazonIVSExtension.initialize()
    }

    fun examples(): List<Example> {
        return listOf(
            Example("Karaoke with Real-Time IVS (Stage)", KaraokeWithIVSRealtimeFragment::class.java as Class<Fragment>),
            Example("Karaoke with Broadcast IVS", KaraokeWithIVSBroadcastFragment::class.java as Class<Fragment>),
            Example("Settings", IVSSettings::class.java as Class<Fragment>),
        )
    }
}

data class Example (
    val title: String,
    var fragment: Class<Fragment>
)