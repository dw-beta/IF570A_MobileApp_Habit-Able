package com.example.uts_lec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementsAdapter(private val achievementsList: List<Map<String, Any?>>) :
    RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievementsList[position]
        val requirement = achievement["requirement"] as Long
        val achievementName = achievement["achievementName"] as String
        val isAchieved = achievement["isAchieved"] as? Boolean ?: false

        holder.achievementRequirement.text = if (isAchieved) "Complete" else "Finish $requirement habit/s"
        holder.achievementName.text = achievementName
        holder.achievementIcon.setImageResource(if (isAchieved) R.drawable.ic_star_true else R.drawable.ic_star_false)
    }

    override fun getItemCount(): Int {
        return achievementsList.size
    }

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val achievementIcon: ImageView = itemView.findViewById(R.id.achievementIcon)
        val achievementRequirement: TextView = itemView.findViewById(R.id.achievementRequirement)
        val achievementName: TextView = itemView.findViewById(R.id.achievementName)
    }
}