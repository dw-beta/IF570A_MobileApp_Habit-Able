package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.example.uts_lec.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FirstDayOfWeekBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_first_day_of_week, container, false)

        val dayPicker = view.findViewById<NumberPicker>(R.id.day_picker)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        val days = arrayOf("Sunday", "Saturday", "Monday")
        dayPicker.minValue = 0
        dayPicker.maxValue = days.size - 1
        dayPicker.displayedValues = days

        cancelButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val selectedDay = days[dayPicker.value]

            // Update the first day of the week text in SettingsFragment
            val parentFragment = parentFragmentManager.findFragmentById(R.id.frame_layout) as GeneralSettingsFragment
            parentFragment.view?.findViewById<TextView>(R.id.first_day_text)?.text = selectedDay

            dismiss()
        }

        return view
    }
}