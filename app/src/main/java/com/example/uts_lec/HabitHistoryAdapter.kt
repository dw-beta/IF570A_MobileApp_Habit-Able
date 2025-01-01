package com.example.uts_lec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitHistoryAdapter(
    private val completedHabits: List<Map<String, Any>>
) : RecyclerView.Adapter<HabitHistoryAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_history, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = completedHabits[position]
        holder.habitNameTextView.text = habit["customHabitName"] as? String ?: "Unknown"
        val completionDate = (habit["completionDate"] as? com.google.firebase.Timestamp)?.toDate()
        holder.habitCompletionDateTextView.text = if (completionDate != null) {
            "Completed on: ${android.text.format.DateFormat.format("yyyy-MM-dd", completionDate)}"
        } else {
            "Completion date unavailable"
        }
    }

    override fun getItemCount() = completedHabits.size

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitNameTextView: TextView = itemView.findViewById(R.id.habitNameTextView)
        val habitCompletionDateTextView: TextView = itemView.findViewById(R.id.habitCompletionDateTextView)
    }
}