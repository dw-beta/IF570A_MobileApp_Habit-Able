package com.example.uts_lec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HabitAdapter(
    private val habitList: MutableList<Map<String, Any?>>, // Use MutableList to allow real-time updates
    private val userId: String // Pass the user ID for easier access
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habitData = habitList[position]
        holder.habitNameTextView.text = habitData["customHabitName"] as String

        // Set up the CheckBox listener
        holder.habitCheckBox.isChecked = habitData["completionStatus"] as? Boolean ?: false
        holder.habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Update Firestore and move the habit
                moveToHabitsSucceeded(habitData, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return habitList.size
    }

    private fun moveToHabitsSucceeded(habitData: Map<String, Any?>, position: Int) {
        val habitId = habitData["habitId"] as String? // Ensure habit ID is in your data structure

        if (habitId != null) {
            val updatedHabitData = habitData.toMutableMap().apply {
                put("completionStatus", true) // Set completionStatus to true
                remove("isCompleted") // Ensure no "isCompleted" field is added
            }

            // Remove from the "habitcreated" collection
            db.collection("habitcreated").document(habitId)
                .delete()
                .addOnSuccessListener {
                    // Add to "habitsucceeded" collection
                    db.collection("habitsucceeded").add(updatedHabitData)
                        .addOnSuccessListener {
                            // Successfully added to "habitsucceeded", now remove from habitList
                            habitList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener {
                            // Handle any errors if necessary
                        }
                }
                .addOnFailureListener {
                    // Handle any errors if necessary
                }
        }
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitNameTextView: TextView = itemView.findViewById(R.id.habitNameTextView)
        val habitCheckBox: CheckBox = itemView.findViewById(R.id.habitCompletedCheckBox)
    }
}
