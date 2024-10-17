package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uts_lec.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class NotificationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MeFragment())
                .addToBackStack(null)
                .commit()
        }

        val reminderSwitch = view.findViewById<SwitchCompat>(R.id.reminder_switch)
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the state of the switch
        }

        // Set the thumb and track tint programmatically
        reminderSwitch.thumbTintList = ContextCompat.getColorStateList(requireContext(), R.color.switch_thumb_color)
        reminderSwitch.trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.switch_track_color)

        val timeSelectionBox = view.findViewById<View>(R.id.time_selection_box)
        timeSelectionBox.setOnClickListener {
            showTimePickerDialog()
        }

        return view
    }

    private fun showTimePickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.time_picker_bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val timePicker = bottomSheetView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)
        timePicker.hour = 12
        timePicker.minute = 0

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        val saveButton = bottomSheetView.findViewById<Button>(R.id.save_button)
        val timeText = view?.findViewById<TextView>(R.id.time_text)

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            timeText?.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}