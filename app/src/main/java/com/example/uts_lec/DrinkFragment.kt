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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DrinkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DrinkFragment : Fragment() {
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_drink, container, false)
        db = FirebaseFirestore.getInstance()

        val startButton = view.findViewById<Button>(R.id.startdrink)
        val journey = view.findViewById<Button>(R.id.backtojourney)

        startButton.setOnClickListener {
            createDrinkHabit()
        }

        journey.setOnClickListener {
            navigateToFragment(JourneyFragment())
        }

        return view
    }

    private fun createDrinkHabit() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if habit already exists
        db.collection("habitcreated")
            .whereEqualTo("userId", userId)
            .whereEqualTo("customHabitName", "Drink Water")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Create new habit
                    val habitData = hashMapOf(
                        "userId" to userId,
                        "customHabitName" to "Drink Water",
                        "description" to "Drink 8 glasses of water daily",
                        "dateCreated" to Date(),
                        "isCompleted" to false,
                        "doItAt" to "Anytime",
                        "color" to "#18C6FD"
                    )

                    db.collection("habitcreated")
                        .add(habitData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Drink Water journey created successfully!", Toast.LENGTH_SHORT).show()
                            navigateToFragment(TodayFragment())
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to create journey: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Drink Water journey already started!", Toast.LENGTH_SHORT).show()
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