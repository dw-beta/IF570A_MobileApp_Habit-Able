package com.example.uts_lec

import Habit
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class CreateHabitFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var habitNameEditText: EditText
    private lateinit var habitDescriptionEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_habit, container, false)

        db = FirebaseFirestore.getInstance()
        habitNameEditText = view.findViewById(R.id.habitNameEditText)
        habitDescriptionEditText = view.findViewById(R.id.habitDescriptionEditText)
        saveButton = view.findViewById(R.id.saveButton)

        val buildCustomHabitButton: Button = view.findViewById(R.id.button4)

        buildCustomHabitButton.setOnClickListener {
            habitNameEditText.visibility = View.VISIBLE
            habitDescriptionEditText.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {
            val habitName = habitNameEditText.text.toString().trim()
            val habitDescription = habitDescriptionEditText.text.toString().trim()
            if (habitName.isNotEmpty() && habitDescription.isNotEmpty()) {
                saveCustomHabitToFirestore(habitName, habitDescription)
            }
            else {
                Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            }
        }
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        return view
    }

    private fun saveCustomHabitToFirestore(name: String, description: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val newHabit = Habit(
                habitName = name,
                description = description,
                dateCreated = Date(),
                dateCompleted = null,
                isCompleted = false,
                userId = userId  // Associate habit with the user’s UID
            )

            FirebaseFirestore.getInstance()
                .collection("users")  // Collection for each user
                .document(userId)  // User document based on UID
                .collection("habits")  // Subcollection for the user's habits
                .add(newHabit)
                .addOnSuccessListener {
                    Toast.makeText(context, "Habit created successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack() // Go back to TodayFragment
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to create habit", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
