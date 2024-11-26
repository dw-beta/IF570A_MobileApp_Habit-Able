package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_lec.databinding.FragmentCreateHabitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class CreateHabitFragment : Fragment() {

    private lateinit var binding: FragmentCreateHabitBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var habitNameEditText: EditText
    private lateinit var habitDescriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var habitAdapter: PremadeHabitAdapter
    private lateinit var habitList: MutableList<PremadeHabit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        habitNameEditText = binding.habitNameEditText
        habitDescriptionEditText = binding.habitDescriptionEditText
        saveButton = binding.saveButton
        val buildCustomHabitButton: Button = binding.button4
        val recyclerView: RecyclerView = binding.premadeHabitRecyclerView

        habitList = mutableListOf()
        habitAdapter = PremadeHabitAdapter(habitList) { habit ->
            savePremadeHabitToFirestore(habit)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = habitAdapter

        buildCustomHabitButton.setOnClickListener {
            val createHabitMenuFragment = CreateHabitMenuFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, createHabitMenuFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        saveButton.setOnClickListener {
            val habitName = habitNameEditText.text.toString().trim()
            val habitDescription = habitDescriptionEditText.text.toString().trim()
            if (habitName.isNotEmpty() && habitDescription.isNotEmpty()) {
                savePremadeHabitToFirestore(PremadeHabit(HabitName = habitName))
            } else {
                Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadPremadeHabits()
    }

    private fun loadPremadeHabits() {
        db.collection("premadehabits")
            .get()
            .addOnSuccessListener { documents ->
                habitList.clear() // Clear the list before adding new items
                for (document in documents) {
                    val habit = document.toObject(PremadeHabit::class.java)
                    habitList.add(habit)
                }
                habitAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load habits", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePremadeHabitToFirestore(habit: PremadeHabit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val newHabit = mapOf(
                "color" to habit.color,
                "completionStatus" to false,
                "customHabitName" to habit.HabitName,
                "dateCreated" to Date(),
                "doItAt" to habit.doItAt,
                "endAt" to habit.endAt,
                "icon" to habit.icon,
                "repeat" to habit.repeat,
                "userId" to userId
            )

            db.collection("habitcreated")
                .add(newHabit)
                .addOnSuccessListener {
                    Toast.makeText(context, "Habit created successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to create habit", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    
    }
}