package com.example.uts_lec

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        saveLocale(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun getLocale(context: Context): Locale {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
        return Locale(languageCode)
    }

    private fun saveLocale(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_language", languageCode)
            apply()
        }
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}