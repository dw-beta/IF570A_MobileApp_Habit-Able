package com.example.uts_lec

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        val colorValue = habitData["color"]

        // Log the retrieved color value
        android.util.Log.d("HabitAdapter", "Retrieved color for habit $habitName: $colorValue")

        var colorHex = colorValue as? String ?: "#18C6FD" // Default to start color if no color is specified

        // Add '#' if not present
        if (!colorHex.startsWith("#")) {
            colorHex = "#$colorHex"
        }

        // Log the final color value
        android.util.Log.d("HabitAdapter", "Final color for habit $habitName: $colorHex")

        // Set the habit name and handle text truncation
        holder.habitNameTextView.text = habitName

        // Show full name in a Toast on tap
        holder.habitNameTextView.setOnClickListener {
            Toast.makeText(holder.itemView.context, habitName, Toast.LENGTH_SHORT).show()
        }

        // Show full name in a Dialog on long-tap
        holder.habitNameTextView.setOnLongClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
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
                markHabitAsCompleted(habitData, position)
            }
        }

        // Set the background color based on the hexadecimal color code
        try {
            holder.itemView.setBackgroundColor(Color.parseColor(colorHex))
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("HabitAdapter", "Invalid color format: $colorHex", e)
        }

        // Handle the ImageButton for the popup menu
        holder.menuButton.setOnClickListener {
            showPopupMenu(holder.menuButton, habitData["habitId"] as String)
        }
    }

    override fun getItemCount(): Int {
        return habitList.size
    }

    private fun markHabitAsCompleted(habitData: Map<String, Any?>, position: Int) {
        val habitId = habitData["habitId"] as String? // Ensure habit ID is in your data structure

        if (habitId != null) {
            val updatedHabitData = habitData.toMutableMap().apply {
                put("completionStatus", true) // Set completionStatus to true
                put("completionDate", com.google.firebase.Timestamp.now()) // Save the current completion date
                put("userId", userId) // Save the user ID
            }

            // Update the habit in the "habitcreated" collection
            db.collection("habitcreated").document(habitId)
                .update(updatedHabitData)
                .addOnSuccessListener {
                    // Add to "habitsucceeded" collection
                    db.collection("habitsucceeded").add(updatedHabitData)
                        .addOnSuccessListener {
                            // Successfully added to "habitsucceeded", now update the habit in habitList
                            if (position < habitList.size) {
                                habitList[position] = updatedHabitData
                                notifyItemChanged(position)
                            }
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

    private fun showPopupMenu(view: View, habitId: String) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.habit_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_habit -> {
                    val fragment = EditHabitMenuFragment().apply {
                        arguments = Bundle().apply {
                            putString("habitId", habitId)
                        }
                    }
                    val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.delete_habit -> {
                    showDeleteConfirmationDialog(view.context, habitId)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showDeleteConfirmationDialog(context: Context, habitId: String) {
        AlertDialog.Builder(context)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteHabit(context, habitId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteHabit(context: Context, habitId: String) {
        FirebaseFirestore.getInstance().collection("habitcreated").document(habitId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete habit", Toast.LENGTH_SHORT).show()
            }
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitNameTextView: TextView = itemView.findViewById(R.id.habitNameTextView)
        val habitCheckBox: CheckBox = itemView.findViewById(R.id.habitCompletedCheckBox)
        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)
    }
}