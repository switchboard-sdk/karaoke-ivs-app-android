package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.utils.AssetLoader
import com.synervoz.switchboardsampleapp.karaokewithivs.config.streamLink
import com.synervoz.switchboardsampleapp.karaokewithivs.config.voices
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentKaraokeWithRealtimeIvsBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentKaraokeWithRealtimeIvsClientBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio.KaraokeWithIVSRealtimeClientExample
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio.KaraokeWithIVSRealtimeExample
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.ContextHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KaraokeWithIVSRealtimeClientFragment : Fragment() {

    companion object {
        val TAG = this::class.java.name
    }

    private lateinit var example: KaraokeWithIVSRealtimeClientExample
    private lateinit var binding: FragmentKaraokeWithRealtimeIvsClientBinding
    var isStreaming = false
    private var frameCallback: Choreographer.FrameCallback? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentKaraokeWithRealtimeIvsClientBinding.inflate(inflater, container, false)

        example = KaraokeWithIVSRealtimeClientExample(requireContext())

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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}