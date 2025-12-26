package com.example.lab3.utils

import android.content.Context
import com.example.lab3.Model.Mood
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefManager(context: Context) {

    private val PREF_NAME = "mood_prefs"
    private val KEY_MOODS = "mood_list"
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun saveMood(mood: Mood) {
        val moods = getMoods().toMutableList()
        moods.removeAll { it.date == mood.date } // Replace if same date
        moods.add(mood)

        val jsonArray = JSONArray()
        for (m in moods) {
            val obj = JSONObject()
            obj.put("date", m.date)
            obj.put("time", m.time)
            obj.put("mood", m.mood)
            obj.put("emoji", m.emoji)
            jsonArray.put(obj)
        }

        editor.putString(KEY_MOODS, jsonArray.toString())
        editor.apply()
    }

    fun getMoods(): List<Mood> {
        val jsonString = sharedPreferences.getString(KEY_MOODS, null) ?: return emptyList()
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Mood>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Mood(
                    obj.getString("date"),
                    obj.getString("time"),
                    obj.getString("mood"),
                    obj.getString("emoji")
                )
            )
        }
        return list
    }

    fun getMoodByDate(date: String): Mood? {
        return getMoods().find { it.date == date }
    }
}