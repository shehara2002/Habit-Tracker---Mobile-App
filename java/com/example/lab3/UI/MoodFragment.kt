package com.example.lab3.UI

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.R
import com.example.lab3.Adapter.MoodAdapter
import com.example.lab3.Model.Mood
import com.example.lab3.utils.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodAdapter
    private lateinit var sharedPrefManager: SharedPrefManager

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val emojiOptions = listOf("😊", "😢", "😡", "😌", "🤩", "😴")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        recyclerView = view.findViewById(R.id.recyclerView)
        sharedPrefManager = SharedPrefManager(requireContext())

        // Setup RecyclerView with MoodAdapter
        adapter = MoodAdapter(sharedPrefManager.getMoods())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Handle date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            showMoodDialog(date)
        }

        return view
    }

    private fun showMoodDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood, null)
        val moodInput = dialogView.findViewById<EditText>(R.id.etMood)
        val emojiSpinner = dialogView.findViewById<Spinner>(R.id.spinnerEmoji)

        // Setup emoji dropdown
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, emojiOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emojiSpinner.adapter = spinnerAdapter

        // If mood already exists for that date, pre-fill fields
        val existingMood = sharedPrefManager.getMoodByDate(date)
        existingMood?.let {
            moodInput.setText(it.mood)
            val index = emojiOptions.indexOf(it.emoji)
            if (index >= 0) emojiSpinner.setSelection(index)
        }

        // Build dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Mood for $date")
        builder.setView(dialogView)

        builder.setPositiveButton("Save") { dialog, _ ->
            val moodText = moodInput.text.toString().trim()
            val selectedEmoji = emojiSpinner.selectedItem.toString()

            if (moodText.isNotEmpty()) {
                val time = timeFormat.format(Date())
                val mood = Mood(date, time, moodText, selectedEmoji)
                sharedPrefManager.saveMood(mood)
                adapter.updateData(sharedPrefManager.getMoods())
                Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
