import java.util.Date

data class Habit(
    val habitName: String = "",
    val description: String = "",
    val dateCreated: Date = Date(),
    val dateCompleted: Date? = null,
    val isCompleted: Boolean = false,
    val userId: String = ""  // Add userId for linking habits to users
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", Date(), null, false, "")
}
