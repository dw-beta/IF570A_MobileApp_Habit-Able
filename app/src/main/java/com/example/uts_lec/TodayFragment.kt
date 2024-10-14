package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class TodayFragment : Fragment() {

    private lateinit var habitInput: EditText
    private lateinit var habitList: TextView
    private val habits = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_today, container, false)

        // Initialize UI elements
        habitInput = view.findViewById(R.id.habitInput)
        habitList = view.findViewById(R.id.habitList)
        val createHabitButton: Button = view.findViewById(R.id.button)

        // Set click listener for the button
        createHabitButton.setOnClickListener {
            val newHabit = habitInput.text.toString().trim()
            if (newHabit.isNotEmpty()) {
                habits.add(newHabit)
                updateHabitList()
                habitInput.text.clear() // Clear input field after adding
            }
        }

        return view
    }

    private fun updateHabitList() {
        // Join the habits into a single string and display them
        habitList.text = habits.joinToString("\n")
    }
}