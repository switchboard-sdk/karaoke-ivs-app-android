package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.utils.AssetLoader
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentKaraokeWithRealtimeIvsBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio.KaraokeAppAudioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KaraokeWithIVSRealtimeFragment : Fragment() {

    private lateinit var example: KaraokeAppAudioEngine
    private lateinit var binding: FragmentKaraokeWithRealtimeIvsBinding
    var isStreaming = false
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentKaraokeWithRealtimeIvsBinding.inflate(inflater, container, false)

        example = KaraokeAppAudioEngine(requireContext())


        binding.startButton.setOnClickListener {
            if (!isStreaming) {
                binding.startButton.text = "Stop Streaming"
                example.startStream()
                isStreaming = true
            } else {
                binding.startButton.text = "Start Streaming"
                example.stopStream()
                isStreaming = false
            }
        }

        binding.musicPlaybackButton.setOnClickListener {
            if (!example.isPlayingMusic()) {
                play()
                binding.musicPlaybackButton.text = "Pause music"
            } else {
                pause()
                binding.musicPlaybackButton.text = "Play music"
            }
        }

        uiScope.launch {
            binding.loadingIndicator.visibility = View.VISIBLE
            withContext(Dispatchers.IO) {
                example.loadAudioFile(
                        requireContext(),
                        "House_of_the_Rising_Sun.mp3"
                )
            }

            example.startAudioEngine()

            binding.loadingIndicator.visibility = View.GONE


            binding.flanagerButton.setOnClickListener {
                example.flangerNode.isEnabled = !example.flangerNode.isEnabled
                if (example.flangerNode.isEnabled)
                    setButtonStateActive(binding.flanagerButton)
                else
                    setButtonStateInactive(binding.flanagerButton)
            }

            binding.reverbButton.setOnClickListener {
                example.reverbNode.isEnabled = !example.reverbNode.isEnabled
                if (example.reverbNode.isEnabled)
                    setButtonStateActive(binding.reverbButton)
                else
                    setButtonStateInactive(binding.reverbButton)
            }

            binding.autotuneButton.setOnClickListener {
                example.autotuneNode.isEnabled = !example.autotuneNode.isEnabled
                if (example.autotuneNode.isEnabled)
                    setButtonStateActive(binding.autotuneButton)
                else
                    setButtonStateInactive(binding.autotuneButton)
            }
        }

        return binding.root
    }

    fun play() {
        example.play()
    }

    fun pause() {
        example.pause()
    }

    private fun setButtonStateInactive(button: Button) {
        button.setBackgroundColor(Color.parseColor("#D3D3D3"))
        button.setTextColor(Color.parseColor("#FF000000"))
    }

    private fun setButtonStateActive(button: Button) {
        button.setBackgroundColor(Color.parseColor("#52b666"))
        button.setTextColor(Color.parseColor("#FFFFFFFF"))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        example.stopAudioEngine()
    }

}