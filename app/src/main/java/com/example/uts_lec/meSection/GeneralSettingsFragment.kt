package com.example.uts_lec.meSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.uts_lec.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeneralSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_settings, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val deleteAllData = view.findViewById<View>(R.id.delete_all_data)
        deleteAllData.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        val timePeriod = view.findViewById<View>(R.id.time_period)
        timePeriod.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(this.id, TimePeriodFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Do you want to delete all data? All your habits and progress will be lost.")
            .setPositiveButton("Yes") { _, _ ->
                deleteAllUserData()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAllUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            val habitCreatedRef = db.collection("habitcreated").whereEqualTo("userId", userId)
            val habitSucceededRef = db.collection("habitsucceeded").whereEqualTo("userId", userId)

            habitCreatedRef.get().addOnSuccessListener { createdDocuments ->
                for (document in createdDocuments) {
                    batch.delete(document.reference)
                }
                habitSucceededRef.get().addOnSuccessListener { succeededDocuments ->
                    for (document in succeededDocuments) {
                        batch.delete(document.reference)
                    }
                    batch.commit().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "All data deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Failed to delete data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}