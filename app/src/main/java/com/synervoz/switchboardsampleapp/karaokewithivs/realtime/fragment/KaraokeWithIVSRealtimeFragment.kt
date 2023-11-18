package com.synervoz.switchboardsampleapp.karaokewithivs.realtime.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.utils.AssetLoader
import com.synervoz.switchboardsampleapp.karaokewithivs.config.streamLink
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentKaraokeWithRealtimeIvsBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.realtime.audio.KaraokeWithIVSRealtimeExample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KaraokeWithIVSRealtimeFragment : Fragment() {

    companion object {
        val TAG = this::class.java.name
    }

    private lateinit var example: KaraokeWithIVSRealtimeExample
    private lateinit var binding: FragmentKaraokeWithRealtimeIvsBinding
    var isStreaming = false
    private var frameCallback: Choreographer.FrameCallback? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentKaraokeWithRealtimeIvsBinding.inflate(inflater, container, false)

        example = KaraokeWithIVSRealtimeExample(requireContext())

        binding.startButton.setOnClickListener {
            if (!isStreaming) {
                binding.startButton.text = "Stop Streaming"
                example.startStream()
                isStreaming = true
                binding.playRecordingButton.visibility = View.GONE
            } else {
                binding.startButton.text = "Start Streaming"
                example.stopStream()
                isStreaming = false
                binding.playRecordingButton.visibility = View.VISIBLE
            }
        }
        binding.playRecordingButton.setOnClickListener {
            if (example.isPlayingRecording()) {
                example.recordingPlayerNode.stop()
                binding.playRecordingButton.text = "Play Recording"
            } else {
                val loaded = example.recordingPlayerNode.load(example.mixedFilePath, Codec.WAV)
                if (loaded) {
                    example.recordingPlayerNode.play()
                    binding.playRecordingButton.text = "Stop Recording"
                }

            }

        }
        binding.voiceVolumeSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                example.setVoiceVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.musicVolumeSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                example.setMusicVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

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
                example.audioPlayerNode.load(
                    AssetLoader.load(
                        requireContext(),
                        "House_of_the_Rising_Sun.mp3"
                    ), Codec.MP3
                )

//                example.vocalPlayerNode.load(
//                    AssetLoader.load(
//                        requireContext(),
//                        "f.mp3"
//                    ), Codec.MP3
//                )


            }
            binding.duration.text =
                "${(example.getSongDurationInSeconds() / 60).toInt()}m " +
                        "${(example.getSongDurationInSeconds() % 60).roundToInt()}s"
            setupProgressBar()
            example.startAudioGraph()
            updateProgressBars()

            binding.loadingIndicator.visibility = View.GONE

            binding.distortionButton.setOnClickListener {
                example.distortionNode.isEnabled = !example.distortionNode.isEnabled
                if (example.distortionNode.isEnabled)
                    setButtonStateActive(binding.distortionButton)
                else
                    setButtonStateInactive(binding.distortionButton)
            }

            binding.vibratoButton.setOnClickListener {
                example.vibratoNode.isEnabled = !example.vibratoNode.isEnabled
                if (example.vibratoNode.isEnabled)
                    setButtonStateActive(binding.vibratoButton)
                else
                    setButtonStateInactive(binding.vibratoButton)
            }

            binding.flanagerButton.setOnClickListener {
                example.flangerNode.isEnabled = !example.flangerNode.isEnabled
                if (example.flangerNode.isEnabled)
                    setButtonStateActive(binding.flanagerButton)
                else
                    setButtonStateInactive(binding.flanagerButton)
            }

            binding.chorusButton.setOnClickListener {
                example.chorusNode.isEnabled = !example.chorusNode.isEnabled
                if (example.chorusNode.isEnabled)
                    setButtonStateActive(binding.chorusButton)
                else
                    setButtonStateInactive(binding.chorusButton)
            }

            binding.autotuneButton.setOnClickListener {
                example.harmonizer.autoTunerisEnabled = !example.harmonizer.autoTunerisEnabled
                if (example.harmonizer.autoTunerisEnabled)
                    setButtonStateActive(binding.autotuneButton)
                else
                    setButtonStateInactive(binding.autotuneButton)
            }

            binding.harmonizerButton.setOnClickListener {
                example.harmonizer.harmonizerIsEnabled = !example.harmonizer.harmonizerIsEnabled
                if (example.harmonizer.harmonizerIsEnabled)
                    setButtonStateActive(binding.harmonizerButton)
                else
                    setButtonStateInactive(binding.harmonizerButton)
            }

            binding.delayButton.setOnClickListener {
                example.delayNode.isEnabled = !example.delayNode.isEnabled
                if (example.delayNode.isEnabled)
                    setButtonStateActive(binding.delayButton)
                else
                    setButtonStateInactive(binding.delayButton)
            }

            binding.reverbButton.setOnClickListener {
                example.reverbNode.isEnabled = !example.reverbNode.isEnabled
                if (example.reverbNode.isEnabled)
                    setButtonStateActive(binding.reverbButton)
                else
                    setButtonStateInactive(binding.reverbButton)
            }
        }

//        val VoiceModadapter =
//            ArrayAdapter(ContextHolder.activity, android.R.layout.simple_spinner_item, voices)
//        binding.spinner.adapter = VoiceModadapter
//
//        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val selectedItem: String = voices[position]
//                if(selectedItem == "none") {
//                    example.voicemodNode.bypassEnabled = true
//                    return
//                }
//                binding.loadingIndicator.visibility = View.VISIBLE
//                uiScope.launch {
//                    withContext(Dispatchers.IO) {
//                        example.voicemodNode.loadVoice(selectedItem)
//                        example.voicemodNode.bypassEnabled = false
//                    }
//                    binding.loadingIndicator.visibility = View.GONE
//                }
//
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//        }


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

    private fun updateUI() {
        if (example.isPlayingMusic()) {
            binding.progressSeekbar.progress =
                (example.getProgress() * binding.progressSeekbar.max).toInt()
            binding.progressLabel.text =
                "${(example.getPositionInSeconds() / 60).toInt()}m " +
                        "${(example.getPositionInSeconds() % 60).roundToInt()}s"
        }
        binding.micRms.progress = (example.vuMeterNode.peak * binding.micRms.max).roundToInt()
    }

    fun setupProgressBar() {
        binding.progressSeekbar.max =
            ((example.getSongDurationInSeconds() * 10).toInt())
        var wasPlaying = false
        binding.progressSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                wasPlaying = example.isPlayingMusic()
                if (wasPlaying) {
                    pause()
                }
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                example.setPositionInSeconds((seekbar!!.progress / 10.0))
                if (wasPlaying) {
                    play()
                }
            }
        })
    }

    fun updateProgressBars() {
        frameCallback = Choreographer.FrameCallback {
            updateUI()
            Choreographer.getInstance().postFrameCallback(frameCallback!!)
        }
        Choreographer.getInstance().postFrameCallback(frameCallback!!)

    }

    fun stopProgressBarUpdate() {
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback!!)
            frameCallback = null
        }
    }

    private fun shareStreamLink() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, "IVS Karaoke Stream Link");
        intent.putExtra(Intent.EXTRA_TEXT, streamLink);
        startActivity(Intent.createChooser(intent, ""))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        example.stopAudioGraph()
        stopProgressBarUpdate()
    }

}