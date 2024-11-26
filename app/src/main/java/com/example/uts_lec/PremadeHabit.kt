package com.example.uts_lec

import java.util.Date

data class PremadeHabit(
    val color: String = "",
    val HabitName: String = "",
    val dateCreated: Date? = null,
    val doItAt: String = "",
    val endAt: String = "",
    val icon: String = "",
    val repeat: String = ""
)