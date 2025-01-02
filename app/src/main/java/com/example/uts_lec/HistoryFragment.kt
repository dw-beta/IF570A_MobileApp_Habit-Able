package com.example.uts_lec

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

private const val TAG = "HistoryFragment"

class HistoryFragment : Fragment() {
    private lateinit var habitHistoryRecyclerView: RecyclerView
    private lateinit var totalHabitsTextView: TextView
    private lateinit var weeklyHabitsTextView: TextView
    private lateinit var completionRateTextView: TextView
    private lateinit var currentStreakTextView: TextView
    private lateinit var bestStreakTextView: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var habitHistoryAdapter: HabitHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val habits = view.findViewById<TextView>(R.id.habits)
        val achievements = view.findViewById<TextView>(R.id.achievements)

        habitHistoryRecyclerView = view.findViewById(R.id.habitHistoryRecyclerView)
        totalHabitsTextView = view.findViewById(R.id.totalHabitsTextView)
        weeklyHabitsTextView = view.findViewById(R.id.weeklyHabitsTextView)
        completionRateTextView = view.findViewById(R.id.completionRateTextView)
        currentStreakTextView = view.findViewById(R.id.currentStreakTextView)
        bestStreakTextView = view.findViewById(R.id.bestStreakTextView)

        fetchHabitStatistics()

        habits.setOnClickListener {
            Log.d(TAG, "Habits clicked")
            navigateToFragment(HistoryFragment())
        }

        achievements.setOnClickListener {
            Log.d(TAG, "Achievements clicked")
            navigateToFragment(AchievementsFragment())
        }

        return view
    }

    private fun fetchHabitStatistics() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("habitsucceeded")
            .whereEqualTo("userId", userId) // Filter by userId
            .get()
            .addOnSuccessListener { querySnapshot ->
                val completedList = querySnapshot.documents.map { it.data ?: emptyMap<String, Any?>() }
                updateCompletedHabits(completedList)
                calculateStatistics(completedList, userId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch completed habits.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCompletedHabits(completedList: List<Map<String, Any>>) {
        val adapter = HabitHistoryAdapter(completedList)
        habitHistoryRecyclerView.adapter = adapter
        habitHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun calculateStatistics(completedList: List<Map<String, Any>?>, userId: String) {
        val completionDates = completedList.mapNotNull {
            (it?.get("completionDate") as? Timestamp)?.toDate()
        }.sorted()

        // Calculate total and weekly habits
        val totalHabits = completedList.size
        val thisWeekCount = completionDates.count {
            isInCurrentWeek(it)
        }

        // Log the total habits collected
        Log.d(TAG, "Total habits collected: $totalHabits")

        // Fetch total habits created
        db.collection("habitcreated").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { createdHabits ->
                val totalCreated = createdHabits.size()
                val completionRate = if (totalCreated > 0) {
                    (totalHabits.toDouble() / totalCreated * 100).toInt()
                } else 0

                // Log the total habits created and the completion rate
                Log.d(TAG, "Total habits created: $totalCreated")
                Log.d(TAG, "Completion rate: $completionRate%")

                completionRateTextView.text = "$completionRate%"
                totalHabitsTextView.text = "$totalHabits"
            }

        // Update streaks
        val streaks = calculateStreaks(completionDates)
        currentStreakTextView.text = "${streaks.first}"
        bestStreakTextView.text = "Best Streak: ${streaks.second}"
        weeklyHabitsTextView.text = "This Week: $thisWeekCount"
    }

    private fun isInCurrentWeek(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.time = date
        val habitWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val habitYear = calendar.get(Calendar.YEAR)

        return currentWeek == habitWeek && currentYear == habitYear
    }

    private fun calculateStreaks(dates: List<Date>): Pair<Int, Int> {
        var currentStreak = 0
        var bestStreak = 0
        var lastDate: Date? = null

        for (date in dates) {
            if (lastDate != null && isConsecutive(lastDate, date)) {
                currentStreak++
            } else {
                bestStreak = maxOf(bestStreak, currentStreak)
                currentStreak = 1
            }
            lastDate = date
        }

        bestStreak = maxOf(bestStreak, currentStreak)
        return Pair(currentStreak, bestStreak)
    }

    private fun isConsecutive(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.time = date1
        calendar2.time = date2
        calendar1.add(Calendar.DAY_OF_YEAR, 1)
        return calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }
}