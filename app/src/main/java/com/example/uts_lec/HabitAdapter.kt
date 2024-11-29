package com.example.uts_lec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
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
        val habitName = habitData["customHabitName"] as String

        // Set the habit name and handle text truncation
        holder.habitNameTextView.text = habitName

        // Show full name in a Toast on tap
        holder.habitNameTextView.setOnClickListener {
            Toast.makeText(holder.itemView.context, habitName, Toast.LENGTH_SHORT).show()
        }

        // Show full name in a Dialog on long-tap
        holder.habitNameTextView.setOnLongClickListener {
            val context = holder.itemView.context
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Habit Name")
                .setMessage(habitName)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            true // Return true to indicate the long click was handled
        }

        // Handle the CheckBox
        holder.habitCheckBox.isChecked = habitData["completionStatus"] as? Boolean ?: false
        holder.habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
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
