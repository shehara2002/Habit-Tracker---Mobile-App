package com.example.lab3.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.R
import com.example.lab3.Model.Mood

class MoodAdapter(private var moodList: List<Mood>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvMood: TextView = itemView.findViewById(R.id.tvMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moodList[position]
        holder.tvEmoji.text = mood.emoji
        holder.tvDate.text = mood.date
        holder.tvTime.text = mood.time
        holder.tvMood.text = mood.mood
    }

    override fun getItemCount(): Int = moodList.size

    fun updateData(newList: List<Mood>) {
        moodList = newList
        notifyDataSetChanged()
    }
}