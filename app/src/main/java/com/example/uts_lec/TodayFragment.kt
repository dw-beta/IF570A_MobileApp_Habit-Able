package com.example.uts_lec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.Date

class TodayFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var emptyTextView: TextView
    private var habitList: MutableList<Map<String, Any?>> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)
        db = FirebaseFirestore.getInstance()
        recyclerViewHabits = view.findViewById(R.id.recyclerViewHabits)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(context)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            habitAdapter = HabitAdapter(habitList, userId) // Pass userId to the adapter
            recyclerViewHabits.adapter = habitAdapter

            // Fetch habits from Firestore
            fetchHabits(userId)
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

        return view
    }

    private fun fetchHabits(userId: String) {
        db.collection("habitcreated")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                habitList.clear() // Clear current list before adding fresh data
                for (document: QueryDocumentSnapshot in result) {
                    val habitName = document.getString("customHabitName") ?: "Unknown Habit"
                    val description = document.getString("description") ?: ""
                    val dateCreated = document.getDate("dateCreated") ?: Date()
                    val dateCompleted = document.getDate("dateCompleted")
                    val isCompleted = document.getBoolean("isCompleted") ?: false

                    // Include the document ID as habitId
                    val habitData = mapOf(
                        "habitId" to document.id,
                        "customHabitName" to habitName,
                        "description" to description,
                        "dateCreated" to dateCreated,
                        "dateCompleted" to dateCompleted,
                        "isCompleted" to isCompleted
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
            .addOnFailureListener {
                // Handle the error if needed
            }
    }

    // Optional: You can also implement a method to remove a habit from the list directly.
    fun removeHabitFromList(habitId: String) {
        val indexToRemove = habitList.indexOfFirst { it["habitId"] == habitId }
        if (indexToRemove != -1) {
            habitList.removeAt(indexToRemove)
            habitAdapter.notifyItemRemoved(indexToRemove)
        }
    }
}

