package com.example.uts_lec.meSection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uts_lec.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class TimePeriodFragment : Fragment() {
    private lateinit var morningStartTime: TextView
    private lateinit var morningEndTime: TextView
    private lateinit var afternoonStartTime: TextView
    private lateinit var afternoonEndTime: TextView
    private lateinit var eveningStartTime: TextView
    private lateinit var eveningEndTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_time_period, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        morningStartTime = view.findViewById(R.id.morning_start_time)
        morningEndTime = view.findViewById(R.id.morning_end_time)
        afternoonStartTime = view.findViewById(R.id.afternoon_start_time)
        afternoonEndTime = view.findViewById(R.id.afternoon_end_time)
        eveningStartTime = view.findViewById(R.id.evening_start_time)
        eveningEndTime = view.findViewById(R.id.evening_end_time)

        // Load saved time periods from SharedPreferences
        loadTimePeriods()

        val morningStartBox = view.findViewById<LinearLayout>(R.id.morning_start_box)
        val morningEndBox = view.findViewById<LinearLayout>(R.id.morning_end_box)
        val afternoonStartBox = view.findViewById<LinearLayout>(R.id.afternoon_start_box)
        val afternoonEndBox = view.findViewById<LinearLayout>(R.id.afternoon_end_box)
        val eveningStartBox = view.findViewById<LinearLayout>(R.id.evening_start_box)
        val eveningEndBox = view.findViewById<LinearLayout>(R.id.evening_end_box)

        morningStartBox.setOnClickListener { showTimePickerDialog(R.id.morning_start_time, container) }
        morningEndBox.setOnClickListener { showTimePickerDialog(R.id.morning_end_time, container) }
        afternoonStartBox.setOnClickListener { showTimePickerDialog(R.id.afternoon_start_time, container) }
        afternoonEndBox.setOnClickListener { showTimePickerDialog(R.id.afternoon_end_time, container) }
        eveningStartBox.setOnClickListener { showTimePickerDialog(R.id.evening_start_time, container) }
        eveningEndBox.setOnClickListener { showTimePickerDialog(R.id.evening_end_time, container) }

        // Set onClickListeners for forward buttons
        view.findViewById<ImageButton>(R.id.forward_button_morning_start).setOnClickListener { showTimePickerDialog(R.id.morning_start_time, container) }
        view.findViewById<ImageButton>(R.id.forward_button_morning_end).setOnClickListener { showTimePickerDialog(R.id.morning_end_time, container) }
        view.findViewById<ImageButton>(R.id.forward_button_afternoon_start).setOnClickListener { showTimePickerDialog(R.id.afternoon_start_time, container) }
        view.findViewById<ImageButton>(R.id.forward_button_afternoon_end).setOnClickListener { showTimePickerDialog(R.id.afternoon_end_time, container) }
        view.findViewById<ImageButton>(R.id.forward_button_evening_start).setOnClickListener { showTimePickerDialog(R.id.evening_start_time, container) }
        view.findViewById<ImageButton>(R.id.forward_button_evening_end).setOnClickListener { showTimePickerDialog(R.id.evening_end_time, container) }

        return view
    }

    private fun loadTimePeriods() {
        val sharedPreferences = requireContext().getSharedPreferences("TimePeriods", Context.MODE_PRIVATE)
        morningStartTime.text = sharedPreferences.getString("morning_start", getString(R.string.morning_start_time))
        morningEndTime.text = sharedPreferences.getString("morning_end", getString(R.string.morning_end_time))
        afternoonStartTime.text = sharedPreferences.getString("afternoon_start", getString(R.string.afternoon_start_time))
        afternoonEndTime.text = sharedPreferences.getString("afternoon_end", getString(R.string.afternoon_end_time))
        eveningStartTime.text = sharedPreferences.getString("evening_start", getString(R.string.evening_start_time))
        eveningEndTime.text = sharedPreferences.getString("evening_end", getString(R.string.evening_end_time))
    }

    private fun showTimePickerDialog(textViewId: Int, container: ViewGroup?) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.time_picker_bottom_sheet, container, false)
        bottomSheetDialog.setContentView(bottomSheetView)

        val timePicker = bottomSheetView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        val saveButton = bottomSheetView.findViewById<Button>(R.id.save_button)
        val timeTextView = view?.findViewById<TextView>(textViewId)

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val newTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

            if (isTimeValid(newTime, textViewId)) {
                timeTextView?.text = newTime
                saveTimePeriod(textViewId, newTime)
            } else {
                Toast.makeText(requireContext(), "Invalid time selection. Please choose a valid time.", Toast.LENGTH_SHORT).show()
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun saveTimePeriod(textViewId: Int, time: String) {
        val sharedPreferences = requireContext().getSharedPreferences("TimePeriods", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        when (textViewId) {
            R.id.morning_start_time -> editor.putString("morning_start", time)
            R.id.morning_end_time -> editor.putString("morning_end", time)
            R.id.afternoon_start_time -> editor.putString("afternoon_start", time)
            R.id.afternoon_end_time -> editor.putString("afternoon_end", time)
            R.id.evening_start_time -> editor.putString("evening_start", time)
            R.id.evening_end_time -> editor.putString("evening_end", time)
        }
        editor.apply()
    }

    private fun isTimeValid(newTime: String, textViewId: Int): Boolean {
        val morningStart = morningStartTime.text.toString()
        val morningEnd = morningEndTime.text.toString()
        val afternoonStart = afternoonStartTime.text.toString()
        val afternoonEnd = afternoonEndTime.text.toString()
        val eveningStart = eveningStartTime.text.toString()
        val eveningEnd = eveningEndTime.text.toString()

        return when (textViewId) {
            R.id.morning_start_time -> newTime < morningEnd && newTime < afternoonStart
            R.id.morning_end_time -> newTime > morningStart && newTime < afternoonStart
            R.id.afternoon_start_time -> newTime > morningEnd && newTime < afternoonEnd && newTime < eveningStart
            R.id.afternoon_end_time -> newTime > afternoonStart && newTime < eveningStart
            R.id.evening_start_time -> newTime > afternoonEnd && newTime < eveningEnd
            R.id.evening_end_time -> newTime > eveningStart
            else -> false
        }
    }
}