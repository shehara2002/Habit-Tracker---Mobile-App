package com.example.lab3.UI

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.Adapter.HabitAdapter
import com.example.lab3.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class HabbitFragment : Fragment() {

    private lateinit var dateTextView: TextView
    private lateinit var saveDateButton: Button
    private lateinit var habitEditText: EditText
    private lateinit var addHabitButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var habitProgress: CircularProgressIndicator
    private lateinit var progressText: TextView

    private val PREFS_NAME = "MyPrefs"
    private val KEY_DATE = "saved_date"
    private val KEY_HABITS = "saved_habits"
    private val KEY_COMPLETED = "completed_habits"

    private var habitList = mutableListOf<String>()
    private var completedSet = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habbit, container, false)

        // Init views
        dateTextView = view.findViewById(R.id.dateTextView)
        saveDateButton = view.findViewById(R.id.saveDateButton)
        habitEditText = view.findViewById(R.id.habitEditText)
        addHabitButton = view.findViewById(R.id.addHabitButton)
        recyclerView = view.findViewById(R.id.habitRecyclerView)
        habitProgress = view.findViewById(R.id.habitProgress)
        progressText = view.findViewById(R.id.progressText)

        // Load data
        loadHabits()
        loadCompletedHabits()
        loadDate()

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HabitAdapter(
            habitList,
            completedSet,
            onDelete = { habit -> deleteHabit(habit) },
            onEdit = { oldHabit, newHabit -> updateHabit(oldHabit, newHabit) },
            onToggleComplete = { habit, isCompleted -> toggleHabitCompletion(habit, isCompleted) }
        )
        recyclerView.adapter = adapter

        updateProgress()

        saveDateButton.setOnClickListener {
            saveDate()
            loadDate()
        }

        addHabitButton.setOnClickListener {
            val habit = habitEditText.text.toString().trim()
            if (habit.isNotEmpty()) {
                habitList.add(habit)
                saveHabits()
                adapter.notifyDataSetChanged()
                habitEditText.text.clear()
                updateProgress()
            } else {
                Toast.makeText(requireContext(), "Please enter a habit", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveDate() {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_DATE, currentDate)
            apply()
        }
    }

    private fun loadDate() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = sharedPreferences.getString(KEY_DATE, "No date saved yet")
        dateTextView.text = "Saved Date: $savedDate"
    }

    private fun saveHabits() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        habitList.forEach { jsonArray.put(it) }
        with(sharedPreferences.edit()) {
            putString(KEY_HABITS, jsonArray.toString())
            apply()
        }
    }

    private fun loadHabits() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString(KEY_HABITS, null)
        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            habitList.clear()
            for (i in 0 until jsonArray.length()) {
                habitList.add(jsonArray.getString(i))
            }
        }
    }

    private fun saveCompletedHabits() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        completedSet.forEach { jsonArray.put(it) }
        with(sharedPreferences.edit()) {
            putString(KEY_COMPLETED, jsonArray.toString())
            apply()
        }
    }

    private fun loadCompletedHabits() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString(KEY_COMPLETED, null)
        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            completedSet.clear()
            for (i in 0 until jsonArray.length()) {
                completedSet.add(jsonArray.getString(i))
            }
        }
    }

    private fun deleteHabit(habit: String) {
        habitList.remove(habit)
        completedSet.remove(habit)
        saveHabits()
        saveCompletedHabits()
        adapter.notifyDataSetChanged()
        updateProgress()
    }

    private fun updateHabit(oldHabit: String, newHabit: String) {
        val index = habitList.indexOf(oldHabit)
        if (index != -1) {
            habitList[index] = newHabit
            if (completedSet.contains(oldHabit)) {
                completedSet.remove(oldHabit)
                completedSet.add(newHabit)
            }
            saveHabits()
            saveCompletedHabits()
            adapter.notifyDataSetChanged()
            updateProgress()
        }
    }

    private fun toggleHabitCompletion(habit: String, isCompleted: Boolean) {
        if (isCompleted) {
            completedSet.add(habit)
        } else {
            completedSet.remove(habit)
        }
        saveCompletedHabits()
        updateProgress()
    }

    private fun updateProgress() {
        val total = habitList.size
        val completed = completedSet.size
        val percent = if (total > 0) (completed * 100) / total else 0
        habitProgress.setProgressCompat(percent, true)
        progressText.text = "Your Progress\n$percent%"
    }
}
