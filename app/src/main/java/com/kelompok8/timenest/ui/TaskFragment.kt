package com.kelompok8.timenest.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import org.json.JSONArray

class TaskFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var endTaskSpinner: Spinner
    private lateinit var startTimeSpinner: Spinner
    private lateinit var endTimeSpinner: Spinner
    private lateinit var remindSpinner: Spinner
    private lateinit var createTaskButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.task_fragment, container, false)

        // Inisialisasi view
        titleInput = view.findViewById(R.id.task_title_input)
        categorySpinner = view.findViewById(R.id.spinner_category)
        endTaskSpinner = view.findViewById(R.id.end_task_spinner)
        startTimeSpinner = view.findViewById(R.id.start_time_spinner)
        endTimeSpinner = view.findViewById(R.id.end_time_spinner)
        remindSpinner = view.findViewById(R.id.remind_spinner)
        createTaskButton = view.findViewById(R.id.btn_create_task)

        // Sample isi spinner
        val endDateOptions = listOf("2025-07-01", "2025-07-02", "2025-07-03")
        val timeOptions = listOf("07:00", "08:00", "09:00")
        val remindOptions = listOf("5 min early", "10 min early", "30 min early")

        endTaskSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, endDateOptions)
        startTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeOptions)
        endTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeOptions)
        remindSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, remindOptions)

        // Ambil kategori dari server
        fetchCategories()

        createTaskButton.setOnClickListener {
            createTask()
        }

        return view
    }

    private fun createTask() {
        val title = titleInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val endDate = endTaskSpinner.selectedItem?.toString() ?: ""
        val startTime = startTimeSpinner.selectedItem?.toString() ?: ""
        val endTime = endTimeSpinner.selectedItem?.toString() ?: ""
        val remind = remindSpinner.selectedItem?.toString() ?: ""

        if (title.isEmpty() || category.isEmpty() || endDate.isEmpty() ||
            startTime.isEmpty() || endTime.isEmpty() || remind.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://10.0.2.2/timenest_api/create_task.php"

        val request = object : StringRequest(Method.POST, url,
            Response.Listener {
                Toast.makeText(requireContext(), "Task berhasil dibuat", Toast.LENGTH_SHORT).show()
                titleInput.setText("")
                categorySpinner.setSelection(0)
                endTaskSpinner.setSelection(0)
                startTimeSpinner.setSelection(0)
                endTimeSpinner.setSelection(0)
                remindSpinner.setSelection(0)
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Gagal membuat task: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId.toString(),
                    "title" to title,
                    "category" to category,
                    "end_date" to endDate,
                    "start_time" to startTime,
                    "end_time" to endTime,
                    "remind" to remind
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchCategories() {
        val url = "http://10.0.2.2/timenest_api/get_categories.php"

        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val categories = mutableListOf<String>()

                    for (i in 0 until jsonArray.length()) {
                        val category = jsonArray.getJSONObject(i).getString("name")
                        categories.add(category)
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categories
                    )
                    categorySpinner.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Gagal parsing kategori", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Gagal mengambil kategori", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }
}

