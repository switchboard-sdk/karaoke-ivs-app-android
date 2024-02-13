package com.synervoz.switchboardsampleapp.karaokewithivs.client.audio

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.BroadcastSession
import com.amazonaws.ivs.broadcast.LocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.DialogHelper
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager

class KaraokeWithIVSRealtimeClientExample(val context: Context) {

    companion object {
        val TAG = this::class.java.name
    }

    var session: BroadcastSession? = null

    private var stage: Stage? = null

    private val stageStrategy = object : Stage.Strategy {
        override fun stageStreamsToPublishForParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): List<LocalStageStream> {
            return listOf()
        }

        override fun shouldPublishFromParticipant(
            stage: Stage,
            participantInfo: ParticipantInfo
        ): Boolean {
            return false
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
        createStage()
    }

    fun startStream() {
        joinStage()
    }

    fun stopStream() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stage?.leave()
        }
        session?.stop()
    }


    private fun createStage() {
        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.CLIENT_TOKEN)
        if (token.isBlank()) {
            DialogHelper.create(context, "Please add your client token in settings!")
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