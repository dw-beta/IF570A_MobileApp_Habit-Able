package com.example.uts_lec.meSection

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uts_lec.R
import com.example.uts_lec.ReminderBroadcastReceiver
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar
import java.util.Locale

class NotificationFragment : Fragment() {

    private lateinit var reminderSwitch: SwitchCompat
    private lateinit var timeText: TextView
    private var selectedHour: Int = 12
    private var selectedMinute: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                scheduleNotification()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission denied to post notifications",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MeSignInFragment())
                .addToBackStack(null)
                .commit()
        }

        reminderSwitch = view.findViewById(R.id.reminder_switch)
        timeText = view.findViewById(R.id.time_text)

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showNotificationPermissionDialog()
            } else {
                cancelNotification()
            }
        }

        timeText.setOnClickListener {
            showTimePickerDialog()
        }

        createNotificationChannel()

        return view
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("Do you want to turn on notifications?")
            .setPositiveButton("Yes") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scheduleNotification()
                    }
                } else {
                    scheduleNotification()
                }
            }
            .setNegativeButton("No") { _, _ ->
                reminderSwitch.isChecked = false
            }
            .show()
    }

    private fun showTimePickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.time_picker_bottom_sheet, view?.parent as ViewGroup, false)
        bottomSheetDialog.setContentView(bottomSheetView)

        val timePicker = bottomSheetView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)
        timePicker.hour = selectedHour
        timePicker.minute = selectedMinute

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        val saveButton = bottomSheetView.findViewById<Button>(R.id.save_button)

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        saveButton.setOnClickListener {
            selectedHour = timePicker.hour
            selectedMinute = timePicker.minute
            timeText.text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            if (reminderSwitch.isChecked) {
                scheduleNotification()
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("habit_reminder_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification() {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java).apply {
            putExtra("encouragementText", "Don't forget to do your daily habit!")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                throw SecurityException("Cannot schedule exact alarms")
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Permission denied to schedule exact alarms",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cancelNotification() {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}