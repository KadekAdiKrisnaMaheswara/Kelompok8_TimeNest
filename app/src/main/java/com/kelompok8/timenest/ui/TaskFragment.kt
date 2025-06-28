package com.kelompok8.timenest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.kelompok8.timenest.R
import com.kelompok8.timenest.data.TaskData
import com.kelompok8.timenest.model.Task

class TaskFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_fragment, container, false)

        val titleInput = view.findViewById<EditText>(R.id.task_title_input)
        val endTaskSpinner = view.findViewById<Spinner>(R.id.end_task_spinner)
        val startTimeSpinner = view.findViewById<Spinner>(R.id.start_time_spinner)
        val endTimeSpinner = view.findViewById<Spinner>(R.id.end_time_spinner)
        val btnCreate = view.findViewById<Button>(R.id.btn_create_task)
        val dates = listOf("Senin, 1 Juli", "Selasa, 2 Juli", "Rabu, 3 Juli")
        val times = listOf("07:00", "08:00", "09:00", "10:00")
        val remindSpinner = view.findViewById<Spinner>(R.id.remind_spinner)
        val remindOptions = listOf("10 min early", "20 min early", "30 min early", "1 hour early")

        endTaskSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dates)
        startTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)
        endTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)
        remindSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, remindOptions)

        btnCreate.setOnClickListener {
            val title = titleInput.text.toString()
            val date = endTaskSpinner.selectedItem?.toString() ?: ""
            val start = startTimeSpinner.selectedItem?.toString() ?: ""
            val end = endTimeSpinner.selectedItem?.toString() ?: ""

            if (title.isNotEmpty()) {
                val newTask = Task(title, date, start, end)
                TaskData.taskList.add(newTask)
                Toast.makeText(requireContext(), "Task created!", Toast.LENGTH_SHORT).show()
                titleInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
