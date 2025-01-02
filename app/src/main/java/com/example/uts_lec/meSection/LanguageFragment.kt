package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.uts_lec.databinding.FragmentLanguageBinding
import com.example.uts_lec.meSection.MeSignInFragment

class LanguageFragment : Fragment() {
    private lateinit var binding: FragmentLanguageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageBinding.inflate(inflater, container, false)

        val backButton = binding.root.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MeSignInFragment())
                .addToBackStack(null)
                .commit()
        }

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