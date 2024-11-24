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
import Habit

class TodayFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var emptyTextView: TextView
    private var habitList: MutableList<Habit> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)
        db = FirebaseFirestore.getInstance()
        recyclerViewHabits = view.findViewById(R.id.recyclerViewHabits)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(context)

        habitAdapter = HabitAdapter(habitList)
        recyclerViewHabits.adapter = habitAdapter

        // Fetch habits from Firestore
        fetchHabits()

        val createHabitButton: Button = view.findViewById(R.id.createHabitButton)
        createHabitButton.setOnClickListener {
            val fragment = CreateHabitFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        return view
    }

    private fun fetchHabits() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users")  // Collection for each user
                .document(userId)  // User document based on UID
                .collection("habits")  // Subcollection for the user's habits
                .get()
                .addOnSuccessListener { result ->
                    habitList.clear()
                    for (document: QueryDocumentSnapshot in result) {
                        val habit = document.toObject(Habit::class.java)
                        habitList.add(habit)
                    }
                    habitAdapter.notifyDataSetChanged()

                    // Show message if habit list is empty
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
        else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}