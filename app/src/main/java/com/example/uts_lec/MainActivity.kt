package com.example.uts_lec

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.uts_lec.databinding.ActivityMainBinding
import com.example.uts_lec.meSection.MeFragment
import com.example.uts_lec.meSection.MeSignInFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        replaceFragment(TodayFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.today -> {
                    replaceFragment(TodayFragment())
                    true
                }
                R.id.journey -> {
                    replaceFragment(JourneyFragment())
                    true
                }
                R.id.history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.me -> {
                    if (isUserSignedIn()) {
                        replaceFragment(MeSignInFragment())
                    } else {
                        replaceFragment(MeFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun isUserSignedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("is_signed_in", false)
    }

    companion object {
        fun setUserSignedIn(context: Context, isSignedIn: Boolean) {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("is_signed_in", isSignedIn)
                apply()
            }
        }
    }
}