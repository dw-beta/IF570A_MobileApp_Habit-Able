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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "AchievementsFragment"

class AchievementsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private var completedHabitsCount: Int = 0
    private lateinit var recyclerViewAchievements: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter
    private var achievementsList: MutableList<Map<String, Any?>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)
        val habits = view.findViewById<TextView>(R.id.habits)
        val achievements = view.findViewById<TextView>(R.id.achievements)

        habits.setOnClickListener {
            Log.d(TAG, "Habits clicked")
            navigateToFragment(HistoryFragment())
        }

        achievements.setOnClickListener {
            Log.d(TAG, "Achievements clicked")
            navigateToFragment(AchievementsFragment())
        }

        recyclerViewAchievements = view.findViewById(R.id.recyclerViewAchievements)
        recyclerViewAchievements.layoutManager = LinearLayoutManager(context) // Use LinearLayoutManager
        achievementsAdapter = AchievementsAdapter(achievementsList)
        recyclerViewAchievements.adapter = achievementsAdapter

        fetchCompletedHabitsCount()

        return view
    }

    private fun fetchCompletedHabitsCount() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d(TAG, "Fetching completed habits for user: $userId")
        db.collection("habitsucceeded")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                completedHabitsCount = documents.size()
                Log.d(TAG, "Completed habits count: $completedHabitsCount")
                checkForAchievements(userId)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting completed habits: ", exception)
            }
    }

    private fun checkForAchievements(userId: String) {
        Log.d(TAG, "Checking achievements for user: $userId")
        db.collection("achievement")
            .get()
            .addOnSuccessListener { documents ->
                achievementsList.clear() // Clear the list to avoid duplicates
                for (document in documents) {
                    val requirement = document.get("requirement")
                    val requirementLong = if (requirement is Number) requirement.toLong() else 0L

                    val achievementData = mapOf(
                        "achievementID" to document.id,
                        "achievementName" to document.getString("achievementName"),
                        "achievementDesc" to document.getString("achievementDesc"),
                        "requirement" to requirementLong
                    )
                    achievementsList.add(achievementData)
                }
                achievementsAdapter.notifyDataSetChanged()
                updateAchievementStatus()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting achievements: ", exception)
            }
    }

    private fun updateAchievementStatus() {
        Log.d(TAG, "Updating achievement status with completed habits count: $completedHabitsCount")
        for (achievement in achievementsList) {
            val requirement = achievement["requirement"] as Long
            if (completedHabitsCount >= requirement) {
                val position = achievementsList.indexOf(achievement)
                achievementsList[position] = achievement.toMutableMap().apply {
                    this["isAchieved"] = true
                }
            }
        }
        achievementsAdapter.notifyDataSetChanged()
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }
}