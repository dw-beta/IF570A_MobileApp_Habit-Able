package com.example.uts_lec

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.uts_lec.databinding.FragmentCreateHabitMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import android.util.Log
import com.example.uts_lec.R

class CreateHabitMenuFragment : Fragment() {

    private var _binding: FragmentCreateHabitMenuBinding? = null
    private val binding get() = _binding!!

    // Variable to hold selected time and color
    private var selectedTime: String = "Anytime"
    private var selectedColor: String = "#0000FF" // Default color (blue)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateHabitMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
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

        binding.iconButton.setOnClickListener {
            showBottomSheetDialog("Icon")
        }

        binding.colorButton.setOnClickListener {
            showBottomSheetDialog("Color")
        }

        binding.anytimeImageButton.setOnClickListener {
            showBottomSheetDialog("Repeat")
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

    private fun saveHabitToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val habitName = binding.habitNameEditText.text.toString().trim()

        if (userId != null && habitName.isNotEmpty()) {
            val habitData = hashMapOf(
                "color" to selectedColor,
                "icon" to "",
                "customHabitName" to habitName,
                "doItAt" to selectedTime,
                "repeat" to getRepeatOption(),
                "endAt" to getEndAtOption(),
                "userId" to userId,
                "dateCreated" to Date(),
                "completionStatus" to false
            )

            FirebaseFirestore.getInstance()
                .collection("habitcreated")
                .add(habitData)
                .addOnSuccessListener {
                    showToast("Habit saved")
                    navigateToTodayFragment()
                }
                .addOnFailureListener {
                    showToast("Failed to save habit")
                }
        } else {
            showToast("User not logged in or habit name is empty")
        }
    }

    private fun getRepeatOption(): String {
        return if (binding.anytimeImageButton.isPressed) {
            "Unlimited"
        } else {
            "Selected Days"
        }
    }

    private fun getEndAtOption(): String {
        return if (binding.endOnSwitch.isChecked) {
            binding.datePicker.toString()
        } else {
            "Unlimited"
        }
    }

    private fun showBottomSheetDialog(type: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = when (type) {
            "Icon" -> layoutInflater.inflate(R.layout.bottom_sheet_icon_change, null)
            "Color" -> layoutInflater.inflate(R.layout.bottom_sheet_color_change, null)
            "Repeat" -> layoutInflater.inflate(R.layout.bottom_sheet_repeat_change, null)
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
        bottomSheetDialog.setContentView(bottomSheetView)

        if (type == "Color") {
            setupColorSelection(bottomSheetView, bottomSheetDialog)
        }

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
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