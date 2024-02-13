package com.synervoz.switchboardsampleapp.karaokewithivs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentSettingsBinding
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceConstants
import com.synervoz.switchboardsampleapp.karaokewithivs.utils.PreferenceManager

class IVSSettings : Fragment() {

    companion object {
        val TAG = this::class.java.name
    }

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val ingestServer =
            PreferenceManager.getGlobalStringPreference(PreferenceConstants.INGEST_SERVER)

        val streamKey =
            PreferenceManager.getGlobalStringPreference(PreferenceConstants.STREAM_KEY)

        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.PUBLISHER_TOKEN)

        val Clienttoken =
            PreferenceManager.getGlobalStringPreference(PreferenceConstants.CLIENT_TOKEN)

        binding.ingestServerEdittext.setText(ingestServer)
        binding.streamKeyEdittext.setText(streamKey)
        binding.tokenEdittext.setText(token)
        binding.listenerTokenEdittext.setText(Clienttoken)

        binding.clearButton.setOnClickListener {
            binding.ingestServerEdittext.text.clear()
            binding.streamKeyEdittext.text.clear()
            binding.tokenEdittext.text.clear()
            binding.listenerTokenEdittext.text.clear()
        }

        binding.saveButton.setOnClickListener {
            PreferenceManager.setGlobalStringPreference(
                PreferenceConstants.INGEST_SERVER,
                binding.ingestServerEdittext.text.toString()
            )
            PreferenceManager.setGlobalStringPreference(
                PreferenceConstants.STREAM_KEY,
                binding.streamKeyEdittext.text.toString()
            )
            PreferenceManager.setGlobalStringPreference(
                PreferenceConstants.PUBLISHER_TOKEN,
                binding.tokenEdittext.text.toString()
            )
            PreferenceManager.setGlobalStringPreference(
                PreferenceConstants.CLIENT_TOKEN,
                binding.listenerTokenEdittext.text.toString()
            )
        }


        return binding.root
    }

}