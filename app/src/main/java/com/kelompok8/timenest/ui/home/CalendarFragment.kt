package com.kelompok8.timenest.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var taskListContainer: LinearLayout
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val readableFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar_fragment, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        selectedDateText = view.findViewById(R.id.selected_date_text)
        taskListContainer = view.findViewById(R.id.taskListContainer)

        // Load task hari ini
        val today = Date()
        updateSelectedDate(today)
        loadTasksForDate(dateFormat.format(today))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val selectedDate = selectedCalendar.time

            updateSelectedDate(selectedDate)
            loadTasksForDate(dateFormat.format(selectedDate))
        }

        return view
    }

    private fun updateSelectedDate(date: Date) {
        val readable = readableFormat.format(date)
        selectedDateText.text = "Tasks on $readable"
    }

    private fun loadTasksForDate(date: String) {
        taskListContainer.removeAllViews()

        val url = "http://10.0.2.2/timenest_api/get_tasks_by_date.php?date=$date"

        val request = StringRequest(
            com.android.volley.Request.Method.GET, url,
            { response ->
                val tasks = JSONArray(response)

                if (tasks.length() == 0) {
                    val emptyView = TextView(requireContext()).apply {
                        text = "No tasks on this date."
                        setPadding(0, 16, 0, 16)
                    }
                    taskListContainer.addView(emptyView)
                } else {
                    for (i in 0 until tasks.length()) {
                        val task = tasks.getJSONObject(i)
                        val title = task.getString("title")
                        val startTime = task.getString("start_time")
                        val endTime = task.getString("end_time")

                        val card = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(24, 16, 24, 16)
                            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                            val titleText = TextView(context).apply {
                                text = title
                                textSize = 16f
                                setPadding(0, 0, 0, 8)
                            }
                            val timeText = TextView(context).apply {
                                text = "$startTime - $endTime"
                                textSize = 14f
                            }
                            addView(titleText)
                            addView(timeText)
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.setMargins(0, 0, 0, 16)
                            setLayoutParams(layoutParams)
                        }

                        taskListContainer.addView(card)
                    }
                }
            },
            {
                val errorView = TextView(requireContext()).apply {
                    text = "Failed to load tasks."
                    setPadding(0, 16, 0, 16)
                }
                taskListContainer.addView(errorView)
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
