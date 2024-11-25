package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.uts_lec.databinding.FragmentCreateHabitMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Date

class CreateHabitMenuFragment : Fragment() {

    private var _binding: FragmentCreateHabitMenuBinding? = null
    private val binding get() = _binding!!

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

        binding.anytimeButton.setOnClickListener {
            onTimeButtonClick(it)
        }

        binding.morningButton.setOnClickListener {
            onTimeButtonClick(it)
        }

        binding.afternoonButton.setOnClickListener {
            onTimeButtonClick(it)
        }

        binding.eveningButton.setOnClickListener {
            onTimeButtonClick(it)
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

    private fun onTimeButtonClick(view: View) {
        binding.anytimeButton.setBackgroundResource(R.drawable.gradient_background)
        binding.morningButton.setBackgroundResource(R.drawable.gradient_background)
        binding.afternoonButton.setBackgroundResource(R.drawable.gradient_background)
        binding.eveningButton.setBackgroundResource(R.drawable.gradient_background)

        view.setBackgroundResource(R.drawable.gradient_background_blue)
    }

    private fun saveHabitToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val habitName = binding.habitNameEditText.text.toString().trim()
        if (userId != null && habitName.isNotEmpty()) {
            val habitData = hashMapOf(
                "color" to "blue",
                "icon" to "",
                "customHabitName" to habitName,
                "doItAt" to getSelectedTime(),
                "repeat" to getRepeatOption(),
                "endAt" to getEndAtOption(),
                "userId" to userId,
                "dateCreated" to Date()
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

    private fun getSelectedTime(): String {
        return when {
            binding.anytimeButton.isPressed -> "Anytime"
            binding.morningButton.isPressed -> "Morning"
            binding.afternoonButton.isPressed -> "Afternoon"
            binding.eveningButton.isPressed -> "Evening"
            else -> "Anytime"
        }
    }

    private fun getRepeatOption(): String {
        // Check if any specific day is selected, otherwise return "Anytime"
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

        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)
        val changeButton = bottomSheetView.findViewById<Button>(R.id.change_button)

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        changeButton.setOnClickListener {
            showToast("$type changed")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTodayFragment() {
        // Begin a new fragment transaction
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        // Replace the current fragment with the HabitsFragment
        transaction.replace(R.id.frame_layout, TodayFragment())
        // Add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null)
        // Commit the transaction to apply the changes
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}