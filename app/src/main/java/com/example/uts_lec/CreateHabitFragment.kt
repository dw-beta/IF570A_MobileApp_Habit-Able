package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CreateHabitFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var habitNameEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_habit, container, false)

        db = FirebaseFirestore.getInstance()
        habitNameEditText = view.findViewById(R.id.habitNameEditText)
        saveButton = view.findViewById(R.id.saveButton)

        // Initialize buttons
        val sleepButton: Button = view.findViewById(R.id.button)
        val workoutButton: Button = view.findViewById(R.id.button2)
        val studyButton: Button = view.findViewById(R.id.button3)
        val buildCustomHabitButton: Button = view.findViewById(R.id.button4)
        val backButton: Button = view.findViewById(R.id.back_button)

        // Click listener for predefined habit buttons
        sleepButton.setOnClickListener {
            saveHabitToFirestore("Sleep Over 8 Hours")
        }

        workoutButton.setOnClickListener {
            saveHabitToFirestore("Workout")
        }

        studyButton.setOnClickListener {
            saveHabitToFirestore("Study Every Day")
        }

        // Show EditText and Save button when "BUILD YOUR OWN CUSTOM HABIT" is clicked
        buildCustomHabitButton.setOnClickListener {
            habitNameEditText.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
        }

        // Save custom habit to Firestore
        saveButton.setOnClickListener {
            val habitName = habitNameEditText.text.toString().trim()
            if (habitName.isNotEmpty()) {
                saveHabitToFirestore(habitName)
            } else {
                Toast.makeText(context, "Habit name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun saveHabitToFirestore(habitName: String) {
        val newHabit = Habit(habitName = habitName)
        db.collection("habits")
            .add(newHabit)
            .addOnSuccessListener {
                Toast.makeText(context, "Habit created successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack() // Go back to TodayFragment
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create habit", Toast.LENGTH_SHORT).show()
            }
    }
}
