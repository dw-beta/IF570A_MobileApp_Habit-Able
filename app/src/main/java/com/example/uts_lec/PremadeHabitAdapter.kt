package com.example.uts_lec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class PremadeHabitAdapter(
    private val habitList: List<PremadeHabit>,
    private val onItemClick: (PremadeHabit) -> Unit
) : RecyclerView.Adapter<PremadeHabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.button4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_premade_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habitList[position]
        holder.button.text = habit.HabitName
        holder.button.setOnClickListener { onItemClick(habit) }
    }

    override fun getItemCount(): Int {
        return habitList.size
    }
}