package com.example.mytodo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var tasksContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

        calendarView = findViewById(R.id.calendarView)
        tasksContainer = findViewById(R.id.tasksContainer)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            displayTasksForDate(selectedDate)
        }

        val BackHome = findViewById<FloatingActionButton>(R.id.floatingActionButton3)
        BackHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayTasksForDate(selectedDate: String) {
        // Clear previous task views
        Log.d("DisplayTasks", "Clearing previous views for date: $selectedDate")
        tasksContainer.removeAllViews()

        val tasksForDate = loadTasksForDate(selectedDate)
        Log.d("DisplayTasks", "Tasks loaded for date $selectedDate: ${tasksForDate.size} task(s) found.")

        if (tasksForDate.isEmpty()) {
            val noTasksTextView = TextView(this).apply {
                text = "No tasks for this date."
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            tasksContainer.addView(noTasksTextView)
        } else {
            for (task in tasksForDate) {
                val cardView = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 16, 0, 16)
                    }
                    radius = 12f
                    elevation = 8f
                    setCardBackgroundColor(getColor(R.color.cardBackground))
                    setPadding(16, 16, 16, 16)
                }

                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 0, 0, 8)
                }

                val textContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val titleTextView = TextView(this).apply {
                    text = task.name
                    textSize = 18f
                    setTextColor(getColor(R.color.titleColor))
                    setPadding(30, 40, 20, 4)
                }

                val dateTextView = TextView(this).apply {
                    text = task.time
                    textSize = 14f
                    setTextColor(getColor(R.color.dateColor))
                    setPadding(35, 30, 25, 8)
                }

                textContainer.addView(titleTextView)
                textContainer.addView(dateTextView)
                layout.addView(textContainer)
                cardView.addView(layout)
                tasksContainer.addView(cardView)
            }
        }
    }

    private fun loadTasks(): List<Task> {
        val sharedPreferences = getSharedPreferences("MyTodoPrefs", Context.MODE_PRIVATE)
        val taskJson = sharedPreferences.getString("task_list", "[]") ?: "[]"
        Log.d("taskJson1", "taskJson: $taskJson")

        // Parse JSON string into a List<Task> using JSONArray and JSONObject
        val taskList = mutableListOf<Task>()
        val jsonArray = JSONArray(taskJson)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("name")
            val date = jsonObject.getString("date")
            val time = jsonObject.getString("time")
            val task = Task(name, date, time)
            taskList.add(task)
        }

        Log.d("taskList", "taskList: $taskList")
        return taskList
    }

    private fun loadTasksForDate(selectedDate: String): List<Task> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())

        val date = inputFormat.parse(selectedDate)
        val formattedDate = if (date != null) outputFormat.format(date) else ""

        val tasks = loadTasks()
        Log.d("tasks232323", "tasks: $tasks")

        val filteredTasks = tasks.filter { it.date == formattedDate }
        Log.d("filteredTasks", "filteredTasks: $filteredTasks")

        return filteredTasks
    }
}

data class Task(val name: String, val date: String, val time: String)
