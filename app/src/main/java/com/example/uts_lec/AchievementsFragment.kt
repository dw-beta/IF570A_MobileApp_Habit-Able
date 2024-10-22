package com.example.uts_lec

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.commit
import org.w3c.dom.Text


private const val TAG = "AchievementsFragment"

class AchievementsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)
        val calendar = view.findViewById<TextView>(R.id.calendar)
        val habits = view.findViewById<TextView>(R.id.habits)
        val achievements = view.findViewById<TextView>(R.id.achievements)

        calendar.setOnClickListener {
            Log.d(TAG, "Calendar clicked")
            navigateToFragment(HistoryFragment())
        }

        habits.setOnClickListener {
            Log.d(TAG, "Habits clicked")
            navigateToFragment(HabitsFragment())
        }

        achievements.setOnClickListener {
            Log.d(TAG, "Achievements clicked")
            navigateToFragment(AchievementsFragment())
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }
}