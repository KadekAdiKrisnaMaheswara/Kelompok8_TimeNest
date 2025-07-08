package com.kelompok8.timenest.ui.home

import android.app.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class TaskFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var startTimeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var remindSpinner: Spinner
    private lateinit var createTaskButton: Button

    private val categories = mutableListOf<Category>()
    private var newCategoryId: Int? = null
    private var isUsingNewCategory = false

    data class Category(val id: Int, val name: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.task_fragment, container, false)

        // Init Views
        titleInput = view.findViewById(R.id.task_title_input)
        categorySpinner = view.findViewById(R.id.spinner_category)
        startDateInput = view.findViewById(R.id.start_task_input)
        endDateInput = view.findViewById(R.id.end_task_input)
        startTimeInput = view.findViewById(R.id.start_time_input)
        endTimeInput = view.findViewById(R.id.end_time_input)
        remindSpinner = view.findViewById(R.id.remind_spinner)
        createTaskButton = view.findViewById(R.id.btn_create_task)

        remindSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("5 min early", "10 min early", "30 min early"))

        fetchCategories()

        startDateInput.setOnClickListener { showDatePickerDialog(startDateInput) }
        endDateInput.setOnClickListener { showDatePickerDialog(endDateInput) }
        startTimeInput.setOnClickListener { showTimePickerDialog(startTimeInput) }
        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }

        createTaskButton.setOnClickListener { createTask() }

        return view
    }

    private fun showDatePickerDialog(target: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            target.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = calendar.timeInMillis
            show()
        }
    }

    private fun showTimePickerDialog(target: EditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hour, minute ->
            target.setText(String.format("%02d:%02d", hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun fetchCategories(selectedId: Int? = null) {
        val url = "http://10.0.2.2/timenest_api/get_categories.php"

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonArray = JSONArray(response)
                categories.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    categories.add(Category(obj.getInt("id"), obj.getString("name")))
                }
                categories.add(Category(-999, "Tambah Kategori Baru"))

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                    categories.map { it.name })
                categorySpinner.adapter = adapter

                categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        val selected = categories[pos]
                        if (selected.id == -999) {
                            showAddCategoryDialog()
                        } else {
                            isUsingNewCategory = false
                            newCategoryId = null
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                selectedId?.let {
                    val index = categories.indexOfFirst { it.id == selectedId }
                    if (index != -1) {
                        // Pakai delay kecil agar binding selesai
                        Handler(Looper.getMainLooper()).postDelayed({
                            categorySpinner.setSelection(index)
                        }, 100)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal parsing kategori", Toast.LENGTH_SHORT).show()
            }
        }, {
            Toast.makeText(requireContext(), "Gagal mengambil kategori", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext())
        input.hint = "Nama kategori baru"

        AlertDialog.Builder(requireContext())
            .setTitle("Kategori Baru")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    createNewCategory(newName)
                } else {
                    Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                categorySpinner.setSelection(0)
            }
            .show()
    }

    private fun createNewCategory(name: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) return

        val url = "http://10.0.2.2/timenest_api/create_category.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val success = obj.optBoolean("success", false)
                    val categoryId = obj.optInt("id", -1)

                    if (success && categoryId != -1) {
                        isUsingNewCategory = true
                        newCategoryId = categoryId

                        Toast.makeText(requireContext(), "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        fetchCategories(categoryId)
                    } else {
                        val errorMsg = obj.optString("message", "Kategori gagal dibuat")
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "Gagal membuat kategori: ${it.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId.toString(),
                    "name" to name
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun createTask() {
        val title = titleInput.text.toString().trim()
        val startDate = startDateInput.text.toString().trim()
        val endDate = endDateInput.text.toString().trim()
        val startTime = startTimeInput.text.toString().trim()
        val endTime = endTimeInput.text.toString().trim()
        val remind = remindSpinner.selectedItem?.toString() ?: ""

        val categoryPosition = categorySpinner.selectedItemPosition
        val selectedCategoryId = if (isUsingNewCategory) newCategoryId ?: -1
        else categories.getOrNull(categoryPosition)?.id ?: -1

        if (title.isEmpty() || selectedCategoryId == -1 || startDate.isEmpty() || endDate.isEmpty()
            || startTime.isEmpty() || endTime.isEmpty() || remind.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://10.0.2.2/timenest_api/create_task.php"
        val request = object : StringRequest(Method.POST, url,
            {
                Toast.makeText(requireContext(), "Task berhasil dibuat", Toast.LENGTH_SHORT).show()
                titleInput.setText("")
                startDateInput.setText("")
                endDateInput.setText("")
                startTimeInput.setText("")
                endTimeInput.setText("")
                remindSpinner.setSelection(0)
                categorySpinner.setSelection(0)
            }, {
                Toast.makeText(requireContext(), "Gagal membuat task: ${it.message}", Toast.LENGTH_SHORT).show()
            }) {
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
