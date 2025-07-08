package com.kelompok8.timenest.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.DatePickerDialog
import android.widget.EditText
import java.util.Calendar
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
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var startTimeSpinner: Spinner
    private lateinit var endTimeSpinner: Spinner
    private lateinit var remindSpinner: Spinner
    private lateinit var createTaskButton: Button

    // List untuk menyimpan id dan nama kategori
    private val categories = mutableListOf<Category>()

    data class Category(val id: Int, val name: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.task_fragment, container, false)

        // Inisialisasi view
        titleInput = view.findViewById(R.id.task_title_input)
        categorySpinner = view.findViewById(R.id.spinner_category)
        startDateInput = view.findViewById(R.id.start_task_input)
        endDateInput = view.findViewById(R.id.end_task_input)
        startTimeSpinner = view.findViewById(R.id.start_time_spinner)
        endTimeSpinner = view.findViewById(R.id.end_time_spinner)
        remindSpinner = view.findViewById(R.id.remind_spinner)
        createTaskButton = view.findViewById(R.id.btn_create_task)

        // Isi data statis spinner lain
        val timeOptions = listOf("07:00", "08:00", "09:00")
        val remindOptions = listOf("5 min early", "10 min early", "30 min early")

        startTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeOptions)
        endTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeOptions)
        remindSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, remindOptions)

        fetchCategories()

        startDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                startDateInput.setText(formattedDate)
            }, year, month, day)

            datePicker.datePicker.minDate = calendar.timeInMillis
            datePicker.show()
        }

        endDateInput.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePicker = android.app.DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Format ke yyyy-MM-dd
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                endDateInput.setText(formattedDate)
            }, year, month, day)

            // Optional: tidak boleh pilih tanggal kemarin
            datePicker.datePicker.minDate = calendar.timeInMillis
            datePicker.show()
        }

        createTaskButton.setOnClickListener {
            createTask()
        }

        return view
    }

    private fun fetchCategories() {
        val url = "http://10.0.2.2/timenest_api/get_categories.php"

        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)
                    categories.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.getInt("id")
                        val name = obj.getString("name")
                        categories.add(Category(id, name))
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categories.map { it.name } // tampilkan nama ke spinner
                    )
                    categorySpinner.adapter = adapter

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal parsing kategori", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Gagal mengambil kategori", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun createTask() {
        val title = titleInput.text.toString().trim()
        val startDate = startDateInput.text.toString().trim()
        val endDate = endDateInput.text.toString().trim()
        val startTime = startTimeSpinner.selectedItem?.toString() ?: ""
        val endTime = endTimeSpinner.selectedItem?.toString() ?: ""
        val remind = remindSpinner.selectedItem?.toString() ?: ""

        val categoryPosition = categorySpinner.selectedItemPosition
        val selectedCategoryId = categories.getOrNull(categoryPosition)?.id ?: -1

        if (title.isEmpty() || selectedCategoryId == -1 || startDate.isEmpty() || endDate.isEmpty()
            || startTime.isEmpty() || endTime.isEmpty() || remind.isEmpty()) {
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
                endDateInput.setText("")
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
                    "category_id" to selectedCategoryId.toString(),
                    "start_date" to startDate,
                    "end_date" to endDate,
                    "start_time" to startTime,
                    "end_time" to endTime,
                    "remind" to remind
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
