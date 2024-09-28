package com.example.mytodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mytodo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.io.Serializable

class AddTaskActivity : AppCompatActivity() {

    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextTaskName: EditText
    private lateinit var submitButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
        editTextTaskName = findViewById(R.id.editTextTaskName)
        submitButton = findViewById(R.id.submitButton)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyTodoPrefs", MODE_PRIVATE)

        // Set click listeners for date and time fields
        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        editTextTime.setOnClickListener {
            showTimePickerDialog()
        }

        val backHome = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        backHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for submit button
        submitButton.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editTextDate.setText(date)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            editTextTime.setText(time)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun saveTask() {
        val taskName = editTextTaskName.text.toString()
        val taskDate = editTextDate.text.toString()
        val taskTime = editTextTime.text.toString()

        if (taskName.isNotEmpty() && taskDate.isNotEmpty() && taskTime.isNotEmpty()) {
            val task = Task(taskName, taskDate, taskTime)

            // Retrieve the current list of tasks from SharedPreferences
            val taskList = getTaskList()
            taskList.add(task)

            // Convert the task list to JSON and save it
            val editor = sharedPreferences.edit()
            val jsonArray = JSONArray()

            for (task in taskList) {
                val taskJson = JSONObject()
                taskJson.put("name", task.name)
                taskJson.put("date", task.date)
                taskJson.put("time", task.time)
                jsonArray.put(taskJson)
            }

            editor.putString("task_list", jsonArray.toString())
            editor.apply()

            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTaskList(): MutableList<Task> {
        val jsonString = sharedPreferences.getString("task_list", null)
        val taskList = mutableListOf<Task>()

        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val taskName = jsonObject.getString("name")
                val taskDate = jsonObject.getString("date")
                val taskTime = jsonObject.getString("time")
                val task = Task(taskName, taskDate, taskTime)
                taskList.add(task)
            }
        }

        return taskList
    }
}

