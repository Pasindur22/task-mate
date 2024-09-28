package com.example.mytodo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteIcon: Drawable
    private val swipeBackground = ColorDrawable(Color.RED)

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_list) // Ensure this is the correct layout file

        sharedPreferences = getSharedPreferences("MyTodoPrefs", MODE_PRIVATE)

        // Initialize the delete icon drawable
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.baseline_delete_sweep_24)!!

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)

        // Load and display tasks using RecyclerView
        loadTasks()

        // Set up BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Floating Action Button to Add Task
        val fabAddTask = findViewById<ImageView>(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        // Implement swipe-to-delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Delete the swiped item from the list and SharedPreferences
                val position = viewHolder.adapterPosition
                deleteTask(position)
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dx: Float,
                dy: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                // Draw background (red)
                swipeBackground.setBounds(
                    itemView.right + dx.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                swipeBackground.draw(canvas)

                // Draw delete icon
                val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconBottom = iconTop + deleteIcon.intrinsicHeight
                val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(canvas)

                super.onChildDraw(canvas, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive)
            }
        }

        // Attach the ItemTouchHelper to the RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        createNotificationChannel(this)

        // Example task at 9:00 AM today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val taskTimeInMillis = calendar.timeInMillis

        // Schedule the task reminder 5 minutes before the task starts
        scheduleTaskReminder(this, taskTimeInMillis)
    }

    private fun loadTasks() {
        val taskList = getTaskList()
        Log.d("taskList", "taskList: $taskList")

        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList) { task ->
            val intent = Intent(this, TaskDetailsActivity::class.java)

            // Convert Task object to JSONObject
            val taskJson = JSONObject().apply {
                put("name", task.name)
                put("date", task.date)
                put("time", task.time)
            }

            // Pass the JSON string as extra
            intent.putExtra("task", taskJson.toString())
            startActivity(intent)
        }
        recyclerView.adapter = taskAdapter

        // Add spacing (e.g., 16dp)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing) // Define in dimens.xml
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTaskReminder(context: Context, taskTimeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm 5 minutes before the task
        // val notificationTime = taskTimeInMillis - 5 * 60 * 1000 // 5 minutes before
        // alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notification Channel"
            val descriptionText = "Channel for task reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getTaskList(): MutableList<Task> {
        val json = sharedPreferences.getString("task_list", null)
        val taskList = mutableListOf<Task>()
        if (json != null) {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val date = jsonObject.getString("date")
                val time = jsonObject.getString("time")
                taskList.add(Task(name, date, time))
            }
        }
        return taskList
    }

    private fun deleteTask(position: Int) {
        // Fetch the current task list from SharedPreferences
        val taskList = getTaskList().toMutableList()

        // Remove the task at the specified position
        if (position in taskList.indices) {
            taskList.removeAt(position)

            // Convert updated task list to JSON manually
            val jsonArray = JSONArray()
            for (task in taskList) {
                val jsonObject = JSONObject()
                jsonObject.put("name", task.name)
                jsonObject.put("date", task.date)
                jsonObject.put("time", task.time)
                jsonArray.put(jsonObject)
            }

            // Save updated task list to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("task_list", jsonArray.toString())
            editor.apply()

            // Update the RecyclerView with the new task list
            taskAdapter.updateTasks(taskList)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "Portrait mode", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Landscape mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the task list to ensure the updated list is shown
        loadTasks()
    }
}
