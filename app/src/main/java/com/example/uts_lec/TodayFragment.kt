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
            habitAdapter = HabitAdapter(habitList, userId)
            recyclerViewHabits.adapter = habitAdapter

            // Fetch habits for the default filter when the fragment loads
            fetchHabits(userId, "Anytime") // Default category, change as needed

            // Set up button listeners
            setupTimeFilterButtons(view, userId)
        }
        else {
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
            .whereEqualTo("doItAt", doItAt) // Filter by doItAt field
            .get()
            .addOnSuccessListener { result ->
                habitList.clear() // Clear the current list
                for (document: QueryDocumentSnapshot in result) {
                    val habitName = document.getString("customHabitName") ?: "Unknown Habit"
                    val description = document.getString("description") ?: ""
                    val dateCreated = document.getDate("dateCreated") ?: Date()
                    val dateCompleted = document.getDate("dateCompleted")
                    val isCompleted = document.getBoolean("isCompleted") ?: false

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
                Toast.makeText(context, "Failed to fetch habits", Toast.LENGTH_SHORT).show()
            }
    }
}
