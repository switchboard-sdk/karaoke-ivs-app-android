package com.synervoz.switchboardsampleapp.karaokewithivs.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.synervoz.switchboardsampleapp.karaokewithivs.databinding.FragmentGuideBinding

class GuideFragment : Fragment() {

    companion object {
        val TAG = this::class.java.name
    }

    private lateinit var binding: FragmentGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

}