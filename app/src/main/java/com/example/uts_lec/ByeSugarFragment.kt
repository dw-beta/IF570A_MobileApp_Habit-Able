package com.example.uts_lec

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ByeSugarFragment : Fragment() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bye_sugar, container, false)

        val startButton = view.findViewById<Button>(R.id.startbyesugar)
        val journey = view.findViewById<Button>(R.id.backtojourney)

        startButton.setOnClickListener {
            createByeSugarHabit()
        }

        journey.setOnClickListener {
            navigateToFragment(JourneyFragment())
        }

        return view
    }

    private fun createByeSugarHabit() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if habit already exists
        db.collection("habitcreated")
            .whereEqualTo("userId", userId)
            .whereEqualTo("customHabitName", "Bye Sugar")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Create new habit
                    val habitData = hashMapOf(
                        "userId" to userId,
                        "customHabitName" to "Bye Sugar",
                        "description" to "Reduce sugar intake for better health",
                        "dateCreated" to Date(),
                        "completionStatus" to false,
                        "doItAt" to "Anytime",
                        "color" to "#FF69B4"  // Using a different color to distinguish it
                    )

                    db.collection("habitcreated")
                        .add(habitData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Bye Sugar journey created successfully!", Toast.LENGTH_SHORT).show()
                            navigateToFragment(TodayFragment())
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to create journey: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Bye Sugar journey already exists!", Toast.LENGTH_SHORT).show()
                    navigateToFragment(TodayFragment())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error checking for existing habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }
}