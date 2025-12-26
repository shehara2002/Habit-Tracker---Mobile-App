package com.example.lab3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.lab3.UI.HabbitFragment
import com.example.lab3.UI.MoodFragment
import com.example.lab3.UI.SettingsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Load default fragment
        loadFragment(HabbitFragment())
        bottomNav.selectedItemId = R.id.nav_habbit

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habbit -> loadFragment(HabbitFragment())
                R.id.nav_mood -> loadFragment(MoodFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())

            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
