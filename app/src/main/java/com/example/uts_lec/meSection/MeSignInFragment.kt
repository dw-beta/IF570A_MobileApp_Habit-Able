package com.example.uts_lec.meSection

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.uts_lec.R
import com.example.uts_lec.database.UserDatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.widget.Button
import com.example.uts_lec.LanguageFragment
import com.example.uts_lec.LoginActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MeFragment"

class MeSignInFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var editIcon: ImageView
    private var photoUri: Uri? = null
    private lateinit var dbHelper: UserDatabaseHelper

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        dbHelper = UserDatabaseHelper(requireContext())

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    profileImageView.setImageURI(uri)
                    savePhotoPathToDatabase(uri.toString())
                }
            }
        }

        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uri ->
                    profileImageView.setImageURI(uri)
                    savePhotoPathToDatabase(uri.toString())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_me_sign_in, container, false)

        profileImageView = view.findViewById(R.id.profile_image)
        userNameTextView = view.findViewById(R.id.user_name)
        editIcon = view.findViewById(R.id.edit_icon)

        val profileLayout = view.findViewById<FrameLayout>(R.id.profile_layout)
        profileLayout.setOnClickListener {
            showImagePickerDialog()
        }
        val notificationLayout = view.findViewById<LinearLayout>(R.id.notification_layout)
        val generalSettingsLayout = view.findViewById<LinearLayout>(R.id.general_settings_layout)
        val languageLayout = view.findViewById<LinearLayout>(R.id.language_layout)
        val shareWithFriendsLayout = view.findViewById<LinearLayout>(R.id.share_with_friends_layout)
        val rateUsLayout = view.findViewById<LinearLayout>(R.id.rate_us_layout)
        val feedbackLayout = view.findViewById<LinearLayout>(R.id.feedback_layout)

        setBackground(notificationLayout, R.color.notification_color, 20f)
        setBackground(generalSettingsLayout, R.color.general_settings_color, 20f)
        setBackground(languageLayout, R.color.language_color, 20f)
        setBackground(shareWithFriendsLayout, R.color.share_with_friends_color, 20f)
        setBackground(rateUsLayout, R.color.rate_us_color, 20f)
        setBackground(feedbackLayout, R.color.feedback_color, 20f)

        notificationLayout.setOnClickListener {
            Log.d(TAG, "Notification clicked")
            navigateToFragment(NotificationFragment())
        }
        generalSettingsLayout.setOnClickListener {
            Log.d(TAG, "General Settings clicked")
            navigateToFragment(GeneralSettingsFragment())
        }
        languageLayout.setOnClickListener {
            Log.d(TAG, "Language clicked")
            navigateToFragment(LanguageFragment())
        }
        shareWithFriendsLayout.setOnClickListener {
            Log.d(TAG, "Share with Friends clicked")
            shareWithFriends()
        }
        rateUsLayout.setOnClickListener {
            Log.d(TAG, "Rate Us clicked")
            rateUs()
        }
        feedbackLayout.setOnClickListener {
            Log.d(TAG, "Feedback clicked")
            feedback()
        }
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        loadUserProfile() // Call the method here

        editIcon.setOnClickListener {
            showEditNameDialog()
        }

        return view
    }

    private fun setBackground(view: LinearLayout, colorResId: Int, cornerRadius: Float) {
        val frameLayout = view.getChildAt(0) as FrameLayout

        val frameGradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                resources.getColor(colorResId, null),
                resources.getColor(colorResId, null)
            )
        )
        frameGradientDrawable.cornerRadius = cornerRadius
        frameLayout.background = frameGradientDrawable
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }

    private fun shareWithFriends() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this amazing app!")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun rateUs() {
        // Implement rate us functionality
    }

    private fun feedback() {
        // Implement feedback functionality
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e(TAG, "Error occurred while creating the file", ex)
            null
        }
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.uts_lec.fileprovider",
                it
            )
            photoUri?.let { uri ->
                takePhotoLauncher.launch(uri)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun savePhotoPathToDatabase(photoPath: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserDatabaseHelper.COLUMN_USER_ID, userId)
            put(UserDatabaseHelper.COLUMN_PHOTO_PATH, photoPath)
        }
        db.insertWithOnConflict(UserDatabaseHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            UserDatabaseHelper.TABLE_NAME,
            arrayOf(UserDatabaseHelper.COLUMN_PHOTO_PATH),
            "${UserDatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            val photoPath = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_PHOTO_PATH))
            profileImageView.setImageURI(Uri.parse(photoPath))
        }
        cursor.close()

        // Fetch and display the user's name from Firestore
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    userNameTextView.text = name ?: getString(R.string.default_user_name)
                } else {
                    userNameTextView.text = getString(R.string.default_user_name)
                }
            }.addOnFailureListener {
                userNameTextView.text = getString(R.string.default_user_name)
            }
        }
    }

    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Name")

        val input = EditText(requireContext())
        input.hint = "Enter new name"
        builder.setView(input)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                updateUserName(newName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateUserName(newName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                userNameTextView.text = newName
                Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update name", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error updating name", e)
            }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeSignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}