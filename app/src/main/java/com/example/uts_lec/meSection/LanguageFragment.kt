package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uts_lec.databinding.FragmentLanguageBinding

class LanguageFragment : Fragment() {
    private lateinit var binding: FragmentLanguageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageBinding.inflate(inflater, container, false)

        binding.englishOption.setOnClickListener {
            setLanguage("en")
        }

        binding.indonesianOption.setOnClickListener {
            setLanguage("id")
        }

        return binding.root
    }

    private fun setLanguage(languageCode: String) {
        LocaleHelper.setLocale(requireContext(), languageCode)
        requireActivity().recreate()
    }
}