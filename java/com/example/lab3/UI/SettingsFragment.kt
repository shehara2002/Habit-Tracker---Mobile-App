package com.example.lab3.UI

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.work.*
import com.example.lab3.R
import com.example.lab3.utils.HydrationWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var timePicker: TimePicker
    private lateinit var selectedTimeText: TextView
    private lateinit var saveButton: Button
    private lateinit var notificationSwitch: Switch
    private lateinit var darkModeSwitch: Switch

    private val PREFS_NAME = "HydrationPrefs"
    private val INTERVAL_HOURS_KEY = "interval_hours"
    private val INTERVAL_MINUTES_KEY = "interval_minutes"
    private val NOTIFICATION_ENABLED_KEY = "notifications_enabled"
    private val DARK_MODE_ENABLED_KEY = "dark_mode_enabled"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        timePicker = view.findViewById(R.id.timePicker)
        selectedTimeText = view.findViewById(R.id.selectedTimeText)
        saveButton = view.findViewById(R.id.saveButton)
        notificationSwitch = view.findViewById(R.id.notificationSwitch)
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // --- Reminder Settings ---
        val savedHours = prefs.getInt(INTERVAL_HOURS_KEY, 1)
        val savedMinutes = prefs.getInt(INTERVAL_MINUTES_KEY, 0)

        timePicker.setIs24HourView(true)
        timePicker.hour = savedHours
        timePicker.minute = savedMinutes

        updateTimeLabel(savedHours, savedMinutes)
        timePicker.setOnTimeChangedListener { _, hour, minute ->
            updateTimeLabel(hour, minute)
        }

        // --- App Settings ---
        val isNotificationEnabled = prefs.getBoolean(NOTIFICATION_ENABLED_KEY, true)
        val isDarkModeEnabled = prefs.getBoolean(DARK_MODE_ENABLED_KEY, false)

        notificationSwitch.isChecked = isNotificationEnabled
        darkModeSwitch.isChecked = isDarkModeEnabled

        // Apply dark mode if saved
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        // Handle Notification Switch
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(NOTIFICATION_ENABLED_KEY, isChecked).apply()
            val message = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Handle Dark Mode Switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DARK_MODE_ENABLED_KEY, isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // --- Save Reminder Button ---
        saveButton.setOnClickListener {
            val selectedHours = timePicker.hour
            val selectedMinutes = timePicker.minute

            prefs.edit()
                .putInt(INTERVAL_HOURS_KEY, selectedHours)
                .putInt(INTERVAL_MINUTES_KEY, selectedMinutes)
                .apply()

            val totalMinutes = (selectedHours * 60L) + selectedMinutes
            if (totalMinutes < 15) {
                Toast.makeText(
                    requireContext(),
                    "Minimum interval should be 15 minutes!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Cancel previous reminders
            val workManager = WorkManager.getInstance(requireContext())
            workManager.cancelAllWorkByTag("hydration_reminder")

            // Schedule new reminder
            val workRequest = PeriodicWorkRequestBuilder<HydrationWorker>(
                totalMinutes, TimeUnit.MINUTES
            ).addTag("hydration_reminder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )

            Toast.makeText(
                requireContext(),
                "Reminder set for every $selectedHours hour(s) $selectedMinutes minute(s)",
                Toast.LENGTH_SHORT
            ).show()
        }

        return view
    }

    private fun updateTimeLabel(hour: Int, minute: Int) {
        selectedTimeText.text = "Every $hour hour(s) $minute minute(s)"
    }
}
