package com.example.uts_lec

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.uts_lec.databinding.FragmentEditHabitMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import android.util.Log
import android.widget.CheckBox

class EditHabitMenuFragment : Fragment() {

    private var _binding: FragmentEditHabitMenuBinding? = null
    private val binding get() = _binding!!

    // Variable to hold selected time and color
    private var selectedTime: String = "Anytime"
    private var selectedColor: String = "#0000FF" // Default color (blue)
    private var habitId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHabitMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannel()

        habitId = arguments?.getString("habitId")
        habitId?.let { loadHabitData(it) }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set listeners for the time buttons
        binding.anytimeButton.setOnClickListener {
            selectTime("Anytime", it)
        }

        binding.morningButton.setOnClickListener {
            selectTime("Morning", it)
        }

        binding.afternoonButton.setOnClickListener {
            selectTime("Afternoon", it)
        }

        binding.eveningButton.setOnClickListener {
            selectTime("Evening", it)
        }

        binding.addButton.setOnClickListener {
            saveHabitToFirestore()
        }

        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.encouragementText.visibility = View.VISIBLE
                binding.timePicker.visibility = View.VISIBLE
                // Request permission when the reminder switch is turned on
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                binding.encouragementText.visibility = View.GONE
                binding.timePicker.visibility = View.GONE
            }
        }

        binding.endOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.endOnText.text = getString(R.string.custom)
                binding.datePicker.visibility = View.VISIBLE
            } else {
                binding.endOnText.text = getString(R.string.unlimited)
                binding.datePicker.visibility = View.GONE
            }
        }

        binding.colorButton.setOnClickListener {
            showBottomSheetDialog("Color", view)
        }

        binding.anytimeImageButton.setOnClickListener {
            showBottomSheetDialog("Repeat", view)
        }
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

    private fun selectTime(time: String, view: View) {
        binding.anytimeButton.setBackgroundResource(R.drawable.gradient_background)
        binding.morningButton.setBackgroundResource(R.drawable.gradient_background)
        binding.afternoonButton.setBackgroundResource(R.drawable.gradient_background)
        binding.eveningButton.setBackgroundResource(R.drawable.gradient_background)

        // Highlight the selected button
        view.setBackgroundResource(R.drawable.gradient_background_blue)

        // Display a Toast with the selected time
        Toast.makeText(view.context, "Selected time: $time", Toast.LENGTH_SHORT).show()
        selectedTime = time
    }

    private fun loadHabitData(habitId: String) {
        FirebaseFirestore.getInstance().collection("habitcreated").document(habitId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val habitData = document.data
                    if (habitData != null) {
                        binding.habitNameEditText.setText(habitData["customHabitName"] as String)
                        selectedColor = habitData["color"] as String
                        selectedTime = habitData["doItAt"] as String
                        binding.anytimeImageButton.contentDescription = habitData["repeat"] as String
                        binding.endOnSwitch.isChecked = habitData["endAt"] != "Unlimited"
                        binding.encouragementText.setText(habitData["encouragementText"] as String)

                        if (habitData["reminderTime"] != null) {
                            binding.reminderSwitch.isChecked = true
                            val reminderTime = habitData["reminderTime"] as String
                            val timeParts = reminderTime.split(":")
                            val hour = timeParts[0].toInt()
                            val minute = timeParts[1].toInt()
                            binding.timePicker.hour = hour
                            binding.timePicker.minute = minute
                        } else {
                            binding.reminderSwitch.isChecked = false
                            binding.encouragementText.visibility = View.GONE
                            binding.timePicker.visibility = View.GONE
                        }

                        updateUI()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load habit data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI() {
        // Update the UI elements based on the loaded data
        when (selectedTime) {
            "Anytime" -> binding.anytimeButton.setBackgroundResource(R.drawable.gradient_background_blue)
            "Morning" -> binding.morningButton.setBackgroundResource(R.drawable.gradient_background_blue)
            "Afternoon" -> binding.afternoonButton.setBackgroundResource(R.drawable.gradient_background_blue)
            "Evening" -> binding.eveningButton.setBackgroundResource(R.drawable.gradient_background_blue)
        }
        // Set other UI elements based on the loaded data...
    }

    private fun saveHabitToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val habitName = binding.habitNameEditText.text.toString().trim()
        val encouragementText = binding.encouragementText.text.toString().trim()
        val reminderText = encouragementText.ifEmpty { "Hey, you have a habit you must do!" }

        if (userId != null && habitName.isNotEmpty()) {
            val habitData = hashMapOf(
                "color" to selectedColor,
                "customHabitName" to habitName,
                "doItAt" to selectedTime,
                "repeat" to getRepeatOption(),
                "endAt" to getEndAtOption(),
                "userId" to userId,
                "dateCreated" to Date(),
                "completionStatus" to false,
                "encouragementText" to reminderText
            )

            if (binding.reminderSwitch.isChecked) {
                habitData["reminderTime"] = getReminderTime()
            }

            habitId?.let {
                FirebaseFirestore.getInstance()
                    .collection("habitcreated")
                    .document(it)
                    .set(habitData)
                    .addOnSuccessListener {
                        showToast("Habit updated")
                        if (binding.reminderSwitch.isChecked) {
                            scheduleNotification(getReminderTime())
                        }
                        navigateToTodayFragment()
                    }
                    .addOnFailureListener {
                        showToast("Failed to update habit")
                    }
            }
        } else {
            showToast("User not logged in or habit name is empty")
        }
    }

    private fun getRepeatOption(): String {
        return binding.anytimeImageButton.contentDescription.toString()
    }

    private fun getEndAtOption(): String {
        return if (binding.endOnSwitch.isChecked) {
            binding.datePicker.toString()
        } else {
            "Unlimited"
        }
    }

    private fun getReminderTime(): String {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute
        return "$hour:$minute"
    }

    private fun scheduleNotification(reminderTime: String) {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java).apply {
            putExtra("encouragementText", binding.encouragementText.text.toString().trim())
        }
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeParts = reminderTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission denied to schedule exact alarms", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBottomSheetDialog(type: String, rootView: View) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = when (type) {
            "Color" -> layoutInflater.inflate(R.layout.bottom_sheet_color_change, rootView as ViewGroup, false)
            "Repeat" -> layoutInflater.inflate(R.layout.bottom_sheet_repeat_change, rootView as ViewGroup, false)
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
        bottomSheetDialog.setContentView(bottomSheetView)

        if (type == "Color") {
            setupColorSelection(bottomSheetView, bottomSheetDialog)
        }
        if (type == "Repeat") {
            setupRepeatSelection(bottomSheetView, bottomSheetDialog)
        }

        bottomSheetDialog.show()
    }

    private fun setupRepeatSelection(view: View, dialog: BottomSheetDialog) {
        val daysOfWeek = listOf<CheckBox>(
            view.findViewById(R.id.monday_checkbox),
            view.findViewById(R.id.tuesday_checkbox),
            view.findViewById(R.id.wednesday_checkbox),
            view.findViewById(R.id.thursday_checkbox),
            view.findViewById(R.id.friday_checkbox),
            view.findViewById(R.id.saturday_checkbox),
            view.findViewById(R.id.sunday_checkbox)
        )

        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            val selectedDays = daysOfWeek.filter { it.isChecked }.joinToString(", ") { it.text.toString() }
            binding.anytimeImageButton.contentDescription = selectedDays.ifEmpty { "Anytime" }
            dialog.dismiss()
        }
    }

    private fun setupColorSelection(view: View, dialog: BottomSheetDialog) {
        val colorButtons = listOf(
            view.findViewById<Button>(R.id.color_red) to "#FF0000",
            view.findViewById<Button>(R.id.color_blue) to "#0000FF",
            view.findViewById<Button>(R.id.color_green) to "#00FF00",
            view.findViewById<Button>(R.id.color_yellow) to "#FFFF00",
            view.findViewById<Button>(R.id.color_purple) to "#800080",
            view.findViewById<Button>(R.id.color_orange) to "#FFA500"
        )

        for ((button, color) in colorButtons) {
            val drawable = button.background as? GradientDrawable
            if (drawable != null) {
                drawable.setColor(Color.parseColor(color)) // Set initial color
                button.background = drawable // Ensure the drawable is set back to the button
                val initialColor = (drawable.color?.defaultColor ?: Color.WHITE).toString()
                Log.d("ColorSelection", "Initial drawable color for button ${button.id}: $initialColor")
            } else {
                Log.e("ColorSelection", "Drawable is null or not a GradientDrawable for button ${button.id}")
            }

            button.setOnClickListener {
                selectedColor = color
                Log.d("ColorSelection", "Selected color: $color")
                if (drawable != null) {
                    drawable.setColor(Color.parseColor(color))
                    Log.d("ColorSelection", "Drawable color set to: $color")
                    button.background = drawable // Ensure the drawable is set back to the button
                } else {
                    Log.e("ColorSelection", "Drawable is null or not a GradientDrawable")
                }
                dialog.dismiss()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTodayFragment() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, TodayFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}