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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.uts_lec.R
import com.example.uts_lec.database.UserDatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.database.sqlite.SQLiteDatabase // Add this import
import android.widget.Button
import com.example.uts_lec.LoginActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MeFragment"
private const val PICK_IMAGE_REQUEST = 1
private const val TAKE_PHOTO_REQUEST = 2

class MeSignInFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private var photoUri: Uri? = null
    private lateinit var dbHelper: UserDatabaseHelper

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_me_sign_in, container, false)

        profileImageView = view.findViewById(R.id.profile_image)
        userNameTextView = view.findViewById(R.id.user_name)

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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
            }
        } else {
            Log.e(TAG, "No camera app available to handle the intent")
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
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        profileImageView.setImageURI(uri)
                        savePhotoPathToDatabase(uri.toString())
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    photoUri?.let { uri ->
                        profileImageView.setImageURI(uri)
                        savePhotoPathToDatabase(uri.toString())
                    }
                }
            }
        }
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