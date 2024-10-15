package com.example.uts_lec

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class CreateHabitFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_habit, container, false)

        // Set up the back button
        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to TodayFragment
            requireActivity().supportFragmentManager.popBackStack()
        }
        return view
    }
}
