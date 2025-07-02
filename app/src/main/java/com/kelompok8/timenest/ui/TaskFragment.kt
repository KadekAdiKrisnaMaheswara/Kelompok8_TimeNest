package com.kelompok8.timenest.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.android.volley.Request
import com.android.volley.Response

class TaskFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.task_fragment, container, false)

        val titleInput = view.findViewById<EditText>(R.id.task_title_input)
        val endTaskSpinner = view.findViewById<Spinner>(R.id.end_task_spinner)
        val startTimeSpinner = view.findViewById<Spinner>(R.id.start_time_spinner)
        val endTimeSpinner = view.findViewById<Spinner>(R.id.end_time_spinner)
        val remindSpinner = view.findViewById<Spinner>(R.id.remind_spinner)
        val btnCreate = view.findViewById<Button>(R.id.btn_create_task)

        val dates = listOf("2025-07-01", "2025-07-02", "2025-07-03") // Gunakan format SQL: YYYY-MM-DD
        val times = listOf("07:00", "08:00", "09:00", "10:00")
        val remindOptions = listOf("10 min early", "20 min early", "30 min early", "1 hour early")

        endTaskSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dates)
        startTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)
        endTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)
        remindSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, remindOptions)

        btnCreate.setOnClickListener {
            val title = titleInput.text.toString()
            val endDate = endTaskSpinner.selectedItem.toString()
            val startTime = startTimeSpinner.selectedItem.toString()
            val endTime = endTimeSpinner.selectedItem.toString()
            val remind = remindSpinner.selectedItem.toString()

            if (title.isNotEmpty()) {
                val url = "http://10.0.2.2/timenest_api/create_task.php"

                val stringRequest = object : StringRequest(
                    Request.Method.POST, url,
                    Response.Listener { response ->
                        Toast.makeText(requireContext(), "Task berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        titleInput.text.clear()
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(requireContext(), "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "title" to title,
                            "end_date" to endDate,
                            "start_time" to startTime,
                            "end_time" to endTime,
                            "remind" to remind
                        )
                    }
                }

                Volley.newRequestQueue(requireContext()).add(stringRequest)

            } else {
                Toast.makeText(requireContext(), "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
