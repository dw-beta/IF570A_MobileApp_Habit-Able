package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.example.uts_lec.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class TimePeriodFragment : Fragment() {
    private lateinit var morningTime: TextView
    private lateinit var afternoonTime: TextView
    private lateinit var eveningTime: TextView
    private lateinit var endOfDayTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_time_period, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        morningTime = view.findViewById(R.id.morning_time)
        afternoonTime = view.findViewById(R.id.afternoon_time)
        eveningTime = view.findViewById(R.id.evening_time)
        endOfDayTime = view.findViewById(R.id.end_of_day_time)

        val morningBox = view.findViewById<LinearLayout>(R.id.morning_box)
        val afternoonBox = view.findViewById<LinearLayout>(R.id.afternoon_box)
        val eveningBox = view.findViewById<LinearLayout>(R.id.evening_box)
        val endOfDayBox = view.findViewById<LinearLayout>(R.id.end_of_day_box)

        morningBox.setOnClickListener { showTimePickerDialog(R.id.morning_time) }
        afternoonBox.setOnClickListener { showTimePickerDialog(R.id.afternoon_time) }
        eveningBox.setOnClickListener { showTimePickerDialog(R.id.evening_time) }
        endOfDayBox.setOnClickListener { showTimePickerDialog(R.id.end_of_day_time) }

        return view
    }

    private fun showTimePickerDialog(timeTextViewId: Int) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.time_picker_bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val timePicker = bottomSheetView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        val saveButton = bottomSheetView.findViewById<Button>(R.id.save_button)
        val timeText = view?.findViewById<TextView>(timeTextViewId)

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val newTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

            if (isTimeValid(newTime, timeTextViewId)) {
                timeText?.text = newTime
            } else {
                // Show error message or handle invalid time
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun isTimeValid(newTime: String, timeTextViewId: Int): Boolean {
        val morning = morningTime.text.toString()
        val afternoon = afternoonTime.text.toString()
        val evening = eveningTime.text.toString()
        val endOfDay = endOfDayTime.text.toString()

        return when (timeTextViewId) {
            R.id.morning_time -> newTime < afternoon && newTime < evening && newTime < endOfDay
            R.id.afternoon_time -> newTime > morning && newTime < evening && newTime < endOfDay
            R.id.evening_time -> newTime > morning && newTime > afternoon && newTime < endOfDay
            R.id.end_of_day_time -> newTime > morning && newTime > afternoon && newTime > evening
            else -> false
        }
    }
}