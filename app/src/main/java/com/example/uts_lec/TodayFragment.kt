package com.example.uts_lec

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

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            habitAdapter = HabitAdapter(habitList, userId)
            recyclerViewHabits.adapter = habitAdapter

            // Fetch habits for the default filter when the fragment loads
            fetchHabits(userId, "Anytime") // Default category, change as needed

            // Set up button listeners
            setupTimeFilterButtons(view, userId)
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        val createHabitButton: Button = view.findViewById(R.id.createHabitButton)
        createHabitButton.setOnClickListener {
            val fragment = CreateHabitFragment()
            fragment.setTargetFragment(this, 0)
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        // Set up the arrow buttons
        setupArrowButtons(view)

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

    private fun setupTimeFilterButtons(view: View, userId: String) {
        val anytimeButton: Button = view.findViewById(R.id.anytimeButton)
        val morningButton: Button = view.findViewById(R.id.morningButton)
        val afternoonButton: Button = view.findViewById(R.id.afternoonButton)
        val eveningButton: Button = view.findViewById(R.id.eveningButton)

        anytimeButton.setOnClickListener {
            fetchHabits(userId, "Anytime")
        }
        morningButton.setOnClickListener {
            fetchHabits(userId, "Morning")
        }
        afternoonButton.setOnClickListener {
            fetchHabits(userId, "Afternoon")
        }
        eveningButton.setOnClickListener {
            fetchHabits(userId, "Evening")
        }
    }

    private fun fetchHabits(userId: String, doItAt: String) {
        db.collection("habitcreated")
            .whereEqualTo("userId", userId)
            .whereEqualTo("doItAt", doItAt)
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