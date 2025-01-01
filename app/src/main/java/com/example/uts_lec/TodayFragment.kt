package com.example.uts_lec

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodayFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var monthNameTextView: TextView
    private var habitList: MutableList<Map<String, Any?>> = mutableListOf()

    private var currentDate: Date = Date() // Initialize with today's date

    private lateinit var anytimeButton: Button
    private lateinit var morningButton: Button
    private lateinit var afternoonButton: Button
    private lateinit var eveningButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)
        db = FirebaseFirestore.getInstance()
        recyclerViewHabits = view.findViewById(R.id.recyclerViewHabits)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(context)
        dateTextView = view.findViewById(R.id.dateTextView)
        monthNameTextView = view.findViewById(R.id.monthNameTextView)

        anytimeButton = view.findViewById(R.id.anytimeButton)
        morningButton = view.findViewById(R.id.morningButton)
        afternoonButton = view.findViewById(R.id.afternoonButton)
        eveningButton = view.findViewById(R.id.eveningButton)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            habitAdapter = HabitAdapter(habitList, userId)
            recyclerViewHabits.adapter = habitAdapter

            // Automatically select the appropriate filter based on the current time
            selectAppropriateFilter(userId)

            // Set up button listeners
            setupTimeFilterButtons(userId)
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        val createHabitButton: Button = view.findViewById(R.id.createHabitButton)
        createHabitButton.setOnClickListener {
            val fragment = CreateHabitFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        // Set up the arrow buttons
        setupArrowButtons(view)

        // Initially display today's date
        updateDateDisplay()

        return view
    }

    private fun setupArrowButtons(view: View) {
        val leftArrowButton: ImageButton = view.findViewById(R.id.leftArrowButton)
        val rightArrowButton: ImageButton = view.findViewById(R.id.rightArrowButton)

        leftArrowButton.setOnClickListener {
            changeDate(-1) // Move one day back
        }

        rightArrowButton.setOnClickListener {
            changeDate(1) // Move one day forward
        }

        // Initially display today's date
        updateDateDisplay()
    }

    private fun changeDate(dayOffset: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset) // Move forward/backward by one day
        currentDate = calendar.time
        updateDateDisplay()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        dateTextView.text = dateFormat.format(currentDate) // Show the full date (day month year)
        monthNameTextView.text = monthFormat.format(currentDate) // Show the month name
    }

    private fun setupTimeFilterButtons(userId: String) {
        anytimeButton.setOnClickListener {
            updateButtonBackgrounds("Anytime")
            fetchHabits(userId, "Anytime")
        }
        morningButton.setOnClickListener {
            updateButtonBackgrounds("Morning")
            fetchHabits(userId, "Morning")
        }
        afternoonButton.setOnClickListener {
            updateButtonBackgrounds("Afternoon")
            fetchHabits(userId, "Afternoon")
        }
        eveningButton.setOnClickListener {
            updateButtonBackgrounds("Evening")
            fetchHabits(userId, "Evening")
        }
    }

    private fun updateButtonBackgrounds(selectedFilter: String) {
        val defaultBackground = R.drawable.gradient_background
        val selectedBackground = R.drawable.gradient_background_blue

        anytimeButton.setBackgroundResource(if (selectedFilter == "Anytime") selectedBackground else defaultBackground)
        morningButton.setBackgroundResource(if (selectedFilter == "Morning") selectedBackground else defaultBackground)
        afternoonButton.setBackgroundResource(if (selectedFilter == "Afternoon") selectedBackground else defaultBackground)
        eveningButton.setBackgroundResource(if (selectedFilter == "Evening") selectedBackground else defaultBackground)
    }

    private fun selectAppropriateFilter(userId: String) {
        val sharedPreferences = requireContext().getSharedPreferences("TimePeriods", Context.MODE_PRIVATE)
        val morningStart = sharedPreferences.getString("morning_start", "09:00") ?: "09:00"
        val morningEnd = sharedPreferences.getString("morning_end", "12:00") ?: "12:00"
        val afternoonStart = sharedPreferences.getString("afternoon_start", "12:00") ?: "12:00"
        val afternoonEnd = sharedPreferences.getString("afternoon_end", "18:00") ?: "18:00"
        val eveningStart = sharedPreferences.getString("evening_start", "15:00") ?: "15:00"
        val eveningEnd = sharedPreferences.getString("evening_end", "23:00") ?: "23:00"

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val doItAt = when {
            currentTime >= morningStart && currentTime < morningEnd -> "Morning"
            currentTime >= afternoonStart && currentTime < afternoonEnd -> "Afternoon"
            currentTime >= eveningStart && currentTime < eveningEnd -> "Evening"
            else -> "Anytime"
        }

        updateButtonBackgrounds(doItAt)
        fetchHabits(userId, doItAt)
    }

    private fun fetchHabits(userId: String, doItAt: String) {
        val sharedPreferences = requireContext().getSharedPreferences("TimePeriods", Context.MODE_PRIVATE)
        val morningStart = sharedPreferences.getString("morning_start", "09:00") ?: "09:00"
        val morningEnd = sharedPreferences.getString("morning_end", "12:00") ?: "12:00"
        val afternoonStart = sharedPreferences.getString("afternoon_start", "12:00") ?: "12:00"
        val afternoonEnd = sharedPreferences.getString("afternoon_end", "18:00") ?: "18:00"
        val eveningStart = sharedPreferences.getString("evening_start", "15:00") ?: "15:00"
        val eveningEnd = sharedPreferences.getString("evening_end", "23:00") ?: "23:00"

        val timeRange = when (doItAt) {
            "Morning" -> morningStart to morningEnd
            "Afternoon" -> afternoonStart to afternoonEnd
            "Evening" -> eveningStart to eveningEnd
            else -> null
        }

        db.collection("habitcreated")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to fetch habits", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (result != null) {
                    habitList.clear()
                    for (document: QueryDocumentSnapshot in result) {
                        val habitName = document.getString("customHabitName") ?: "Unknown Habit"
                        val description = document.getString("description") ?: ""
                        val dateCreated = document.getDate("dateCreated") ?: Date()
                        val dateCompleted = document.getDate("dateCompleted")
                        val isCompleted = document.getBoolean("isCompleted") ?: false
                        val color = document.getString("color") ?: "#18C6FD" // Default color if not specified
                        val doItAtTime = document.getString("doItAtTime") ?: "00:00"

                        if (timeRange == null || (doItAtTime >= timeRange.first && doItAtTime < timeRange.second)) {
                            val habitData = mapOf(
                                "habitId" to document.id,
                                "customHabitName" to habitName,
                                "description" to description,
                                "dateCreated" to dateCreated,
                                "dateCompleted" to dateCompleted,
                                "isCompleted" to isCompleted,
                                "color" to color // Add the color field
                            )
                            habitList.add(habitData)
                        }
                    }
                    habitAdapter.notifyDataSetChanged()

                    if (habitList.isEmpty()) {
                        recyclerViewHabits.visibility = View.GONE
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        recyclerViewHabits.visibility = View.VISIBLE
                        emptyTextView.visibility = View.GONE
                    }
                }
            }
    }
}