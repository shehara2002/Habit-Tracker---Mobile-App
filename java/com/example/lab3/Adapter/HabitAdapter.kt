package com.example.lab3.Adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.R

class HabitAdapter(
    private val habits: MutableList<String>,
    private val completedSet: MutableSet<String>,
    private val onDelete: (String) -> Unit,
    private val onEdit: (String, String) -> Unit,
    private val onToggleComplete: (String, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val habitText: TextView = view.findViewById(R.id.habitText)
        val editButton: ImageView = view.findViewById(R.id.editButton)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
        val tickButton: ImageView = view.findViewById(R.id.tickButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val context = holder.itemView.context
        val isCompleted = completedSet.contains(habit)

        // Update habit text and opacity
        holder.habitText.text = habit
        holder.habitText.alpha = if (isCompleted) 0.6f else 1f

        // Update tick button visual
        holder.tickButton.setImageResource(
            if (isCompleted) R.drawable.ic_check else R.drawable.ic_circle
        )
        holder.tickButton.setColorFilter(
            ContextCompat.getColor(
                context,
                if (isCompleted) R.color.black else R.color.purple_500
            )
        )

        // Tick button toggles completion
        holder.tickButton.setOnClickListener {
            val nowCompleted = !completedSet.contains(habit)
            if (nowCompleted) {
                completedSet.add(habit)
            } else {
                completedSet.remove(habit)
            }
            onToggleComplete(habit, nowCompleted)
            notifyItemChanged(position) // only update this item
        }

        //  Edit button
        holder.editButton.setOnClickListener {
            val editText = EditText(context)
            editText.setText(habit)

            AlertDialog.Builder(context)
                .setTitle("Edit Habit")
                .setView(editText)
                .setPositiveButton("Update") { _, _ ->
                    val newName = editText.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        onEdit(habit, newName)
                        notifyItemChanged(position)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //  Delete button
        holder.deleteButton.setOnClickListener {
            onDelete(habit)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = habits.size
}
