package com.synervoz.switchboardsampleapp.karaokewithivs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.synervoz.switchboardsampleapp.karaokewithivs.config.ingestServer
import com.synervoz.switchboardsampleapp.karaokewithivs.config.streamKey
import com.synervoz.switchboardsampleapp.karaokewithivs.config.token
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
                ?: ingestServer
        val streamKey =
            PreferenceManager.getGlobalStringPreference(PreferenceConstants.STREAM_KEY) ?: streamKey
        val token = PreferenceManager.getGlobalStringPreference(PreferenceConstants.TOKEN) ?: token

        binding.ingestServerEdittext.setText(ingestServer)
        binding.streamKeyEdittext.setText(streamKey)
        binding.tokenEdittext.setText(token)

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
                PreferenceConstants.TOKEN,
                binding.tokenEdittext.text.toString()
            )
        }


        return binding.root
    }

}