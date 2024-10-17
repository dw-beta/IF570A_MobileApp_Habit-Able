package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.uts_lec.R

class GeneralSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_settings, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val firstDayOfWeek = view.findViewById<View>(R.id.first_day_of_week)
        firstDayOfWeek.setOnClickListener {
            val bottomSheet = FirstDayOfWeekBottomSheet()
            bottomSheet.show(parentFragmentManager, "FirstDayOfWeekBottomSheet")
        }

        val timePeriod = view.findViewById<View>(R.id.time_period)
        timePeriod.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, TimePeriodFragment())
                .addToBackStack(null)
                .commit()
        }

        val privacyPolicy = view.findViewById<View>(R.id.privacy_policy)
        privacyPolicy.setOnClickListener {
            // Handle privacy policy click
        }

        val deleteAllData = view.findViewById<View>(R.id.delete_all_data)
        deleteAllData.setOnClickListener {
            // Handle delete all data click
        }

        return view
    }
}