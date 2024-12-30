package com.example.uts_lec.meSection

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.uts_lec.R
import android.app.AlertDialog

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MeFragment"

class MeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_me, container, false)

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

        return view
    }

    private fun setBackground(view: LinearLayout, colorResId: Int, cornerRadius: Float) {
        val frameLayout = view.getChildAt(0) as FrameLayout

        // Set the background color of the frame layout (logo background small box)
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}