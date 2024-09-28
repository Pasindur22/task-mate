package com.example.mytodo

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var etTimerInput: EditText
    private lateinit var btnStartTimer: Button
    private lateinit var btnStopTimer: Button
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details) // Ensure this layout file exists

        val taskJsonString = intent.getStringExtra("task")

        if (taskJsonString != null) {
            try {
                val taskJson = JSONObject(taskJsonString)
                val taskName = taskJson.getString("name")
                val taskDate = taskJson.getString("date")
                val taskTime = taskJson.getString("time")

                // Set the task data to TextViews
                findViewById<TextView>(R.id.tvTaskName).text = taskName
                findViewById<TextView>(R.id.tvDate).text = taskDate
                findViewById<TextView>(R.id.tvTime).text = taskTime
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show()
            }
        }

        timerTextView = findViewById(R.id.tvTimer)
        etTimerInput = findViewById(R.id.etTimerInput)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnStopTimer = findViewById(R.id.btnStopTimer)

        btnStartTimer.setOnClickListener { startTimer() }
        btnStopTimer.setOnClickListener { stopTimer() }
    }

    private fun startTimer() {
        val input = etTimerInput.text.toString()
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter time in minutes", Toast.LENGTH_SHORT).show()
            return
        }

        val timeInMinutes = input.toLong()
        val timeInMillis = timeInMinutes * 60 * 1000

        countDownTimer?.cancel() // Cancel any existing timer
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000) % 60
                val minutesRemaining = (millisUntilFinished / 1000) / 60
                timerTextView.text = String.format("%02d:%02d", minutesRemaining, secondsRemaining)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                Toast.makeText(this@TaskDetailsActivity, "Timer finished!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel() // Cancel the timer
        timerTextView.text = "00:00" // Reset the timer display
        Toast.makeText(this, "Timer stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Cancel timer if activity is destroyed
    }
}
