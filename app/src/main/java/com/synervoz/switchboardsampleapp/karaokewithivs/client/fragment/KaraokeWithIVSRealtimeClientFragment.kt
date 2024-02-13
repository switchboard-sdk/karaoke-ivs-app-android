package com.synervoz.switchboardsampleapp.karaokewithivs.client.fragment

import android.os.Bundle
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentKaraokeWithRealtimeIvsClientBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.client.audio.KaraokeWithIVSRealtimeClientExample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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