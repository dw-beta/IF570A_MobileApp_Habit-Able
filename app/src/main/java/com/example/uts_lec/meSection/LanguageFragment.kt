package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.uts_lec.R

class LanguageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_language, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MeFragment())
                .addToBackStack(null)
                .commit()
        }

        val englishOption = view.findViewById<LinearLayout>(R.id.english_option)
        val indonesianOption = view.findViewById<LinearLayout>(R.id.indonesian_option)
        val checkEnglish = view.findViewById<ImageView>(R.id.check_english)
        val checkIndonesian = view.findViewById<ImageView>(R.id.check_indonesian)

        englishOption.setOnClickListener {
            checkEnglish.visibility = View.VISIBLE
            checkIndonesian.visibility = View.GONE
            // Add logic to change the app language to English
        }

        indonesianOption.setOnClickListener {
            checkEnglish.visibility = View.GONE
            checkIndonesian.visibility = View.VISIBLE
            // Add logic to change the app language to Indonesian
        }

        return view
    }
}