package com.example.uts_lec

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var nameEditText: EditText
    private lateinit var idNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.setLocale(newBase, LocaleHelper.getLocale(newBase).language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText)
        idNumberEditText = findViewById(R.id.idNumberEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val idNumber = idNumberEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (validateInput(name, idNumber, email, password, confirmPassword)) {
                registerUser(name, idNumber, email, password)
            }
        }

        loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, idNumber: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return false
        }
        if (idNumber.isEmpty()) {
            idNumberEditText.error = "ID Number is required"
            return false
        }
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return false
        }
        return true
    }

    private fun registerUser(name: String, idNumber: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save additional user info to Firestore
                    val user = auth.currentUser
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(user?.uid ?: "")
                    val userData = hashMapOf(
                        "name" to name,
                        "idNumber" to idNumber,
                        "email" to email
                    )

                    userRef.set(userData).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Failed to save user data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}