package com.example.mytodo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable

class TaskAdapter(
    private var taskList: List<Task>,
    private val onTaskClick: (Task) -> Unit // Click listener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskName)
        val taskDateTextView: TextView = itemView.findViewById(R.id.taskDate)
        val taskTimeTextView: TextView = itemView.findViewById(R.id.taskTime)
        val editIcon: ImageView = itemView.findViewById(R.id.imageViewEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskNameTextView.text = task.name
        holder.taskDateTextView.text = task.date
        holder.taskTimeTextView.text = task.time

        // Set the click listener for the task item
        holder.itemView.setOnClickListener {
            onTaskClick(task) // Trigger the click listener
        }

        holder.editIcon.setOnClickListener {
            // Create an intent to navigate to the EditTaskActivity
            val context = holder.itemView.context
            val intent = Intent(context, EditTask::class.java)

            // Pass the task object as Serializable
            intent.putExtra("task", task as Serializable)
            intent.putExtra("task_position", position)

            // Start the EditTaskActivity
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = taskList.size

    fun updateTasks(newTasks: List<Task>) {
        taskList = newTasks  // Replace the old list with the new one
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
