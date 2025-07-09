package com.kelompok8.timenest.ui.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.kelompok8.timenest.data.DatabaseHelper
import com.kelompok8.timenest.model.GroupedTask
import com.kelompok8.timenest.model.Task
import com.kelompok8.timenest.ui.GroupedCategoryAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var tvWelcome: TextView
    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: ImageView

    private val groupedList = mutableListOf<GroupedTask>()
    private val fullTaskList = mutableListOf<Task>()

    private lateinit var groupedCategoryAdapter: GroupedCategoryAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        tvWelcome = rootView.findViewById(R.id.tvWelcome)
        searchInput = rootView.findViewById(R.id.search_input)
        clearSearchButton = rootView.findViewById(R.id.btn_clear_search)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", null)
        val greetingName = if (!name.isNullOrBlank()) name else getString(R.string.default_user_name)
        tvWelcome.text = getString(R.string.greeting, greetingName)

        val btnAddCategory = rootView.findViewById<Button>(R.id.btn_add_category)
        btnAddCategory.setOnClickListener { showAddCategoryDialog() }

        recyclerCategories = rootView.findViewById(R.id.recyclerCategories)
        recyclerCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        groupedCategoryAdapter = GroupedCategoryAdapter(groupedList) { updateTaskList(it) }
        recyclerCategories.adapter = groupedCategoryAdapter

        recyclerTasks = rootView.findViewById(R.id.recyclerViewTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(mutableListOf(), { showEditTaskDialog(it) }, { confirmDeleteTask(it) })
        recyclerTasks.adapter = taskAdapter

        fetchTasksFromServer()
        setupSearchListener()

        return rootView
    }

    private fun fetchTasksFromServer() {
        val userId = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) return

        val url = "http://10.0.2.2/timenest_api/get_task.php?user_id=$userId"

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonArray = JSONArray(response)
                val taskMap = mutableMapOf<String, MutableList<Task>>()
                fullTaskList.clear()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val categoryName = obj.optString("category") ?: "Uncategorized"
                    val task = Task(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        category = if (categoryName == "null" || categoryName.isBlank()) "Uncategorized" else categoryName,
                        startDate = obj.optString("start_date", ""),
                        endDate = obj.getString("end_date"),
                        startTime = obj.getString("start_time"),
                        endTime = obj.getString("end_time"),
                        remind = obj.getString("remind")
                    )
                    fullTaskList.add(task)
                    taskMap.getOrPut(task.category) { mutableListOf() }.add(task)
                }

                groupedList.clear()
                for ((category, tasks) in taskMap) {
                    groupedList.add(GroupedTask(category, tasks))
                }

                groupedCategoryAdapter.notifyDataSetChanged()
                updateTaskList(fullTaskList)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Gagal parsing task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            Toast.makeText(requireContext(), "Gagal mengambil task: ${error.message}", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun updateTaskList(tasks: List<Task>) {
        taskAdapter.updateData(tasks)
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                clearSearchButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filterTasks(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            updateTaskList(fullTaskList)
        }
    }

    private fun filterTasks(query: String) {
        val filtered = fullTaskList.filter {
            it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
        }
        updateTaskList(filtered)
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null)

        val edtTitle = dialogView.findViewById<EditText>(R.id.editTaskTitle)
        val edtStartDate = dialogView.findViewById<EditText>(R.id.editTaskStartDate)
        val edtEndDate = dialogView.findViewById<EditText>(R.id.editTaskEndDate)
        val edtStartTime = dialogView.findViewById<EditText>(R.id.editTaskStartTime)
        val edtEndTime = dialogView.findViewById<EditText>(R.id.editTaskEndTime)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_edit_category)
        val spinnerRemind = dialogView.findViewById<Spinner>(R.id.spinner_edit_remind)

        // Declare category list and map outside the request
        val categoryList = mutableListOf<String>()
        val categoryMap = mutableMapOf<String, Int>()

        val userId = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("user_id", -1)
        val categoryUrl = "http://10.0.2.2/timenest_api/get_categories.php?user_id=$userId"

        val queue = Volley.newRequestQueue(requireContext())
        val request = StringRequest(Request.Method.GET, categoryUrl, { response ->
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val name = obj.getString("name")
                categoryList.add(name)
                categoryMap[name] = id
            }

            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = categoryAdapter

            val categoryIndex = categoryList.indexOf(task.category)
            if (categoryIndex != -1) spinnerCategory.setSelection(categoryIndex)

        }, {
            Toast.makeText(requireContext(), "Gagal ambil kategori", Toast.LENGTH_SHORT).show()
        })

        queue.add(request)

        val remindOptions = listOf("None", "5 min early", "10 min early", "30 min early", "1 hour early")
        val remindAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, remindOptions)
        remindAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRemind.adapter = remindAdapter
        val remindIndex = remindOptions.indexOf(task.remind)
        if (remindIndex != -1) spinnerRemind.setSelection(remindIndex)

        edtTitle.setText(task.title)
        edtStartDate.setText(task.startDate)
        edtEndDate.setText(task.endDate)
        edtStartTime.setText(task.startTime)
        edtEndTime.setText(task.endTime)

        edtStartDate.setOnClickListener { showDatePicker(edtStartDate) }
        edtEndDate.setOnClickListener { showDatePicker(edtEndDate) }
        edtStartTime.setOnClickListener { showTimePicker(edtStartTime) }
        edtEndTime.setOnClickListener { showTimePicker(edtEndTime) }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Simpan", null) // nanti kita handle klik manual biar bisa akses Spinner
            .setNegativeButton("Batal", null)
            .create()

        alertDialog.setOnShowListener {
            val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val newTitle = edtTitle.text.toString().trim()
                val newStartDate = edtStartDate.text.toString().trim()
                val newEndDate = edtEndDate.text.toString().trim()
                val newStartTime = edtStartTime.text.toString().trim()
                val newEndTime = edtEndTime.text.toString().trim()
                val newRemind = spinnerRemind.selectedItem.toString()
                val selectedCategoryName = spinnerCategory.selectedItem?.toString() ?: "Uncategorized"
                val selectedCategoryId = categoryMap[selectedCategoryName] ?: 0

                updateTask(task.id, newTitle, newStartDate, newEndDate, newStartTime, newEndTime, newRemind, selectedCategoryId)
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun updateTask(
        taskId: Int,
        title: String,
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String,
        remind: String,
        categoryId: Int
    ) {
        val url = "http://10.0.2.2/timenest_api/update_task.php"

        val request = object : StringRequest(Request.Method.POST, url, {
            Toast.makeText(requireContext(), "Task berhasil diupdate", Toast.LENGTH_SHORT).show()
            fetchTasksFromServer()
        }, {
            Toast.makeText(requireContext(), "Gagal update task: ${it.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id" to taskId.toString(),
                    "title" to title,
                    "start_date" to startDate,
                    "end_date" to endDate,
                    "start_time" to startTime,
                    "end_time" to endTime,
                    "remind" to remind,
                    "category_id" to categoryId.toString()  // penting!
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }


    private fun confirmDeleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Task")
            .setMessage("Apakah yakin ingin menghapus task '${task.title}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteTask(task.id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTask(taskId: Int) {
        val url = "http://10.0.2.2/timenest_api/delete_task.php"

        val request = object : StringRequest(Request.Method.POST, url, {
            Toast.makeText(requireContext(), "Task berhasil dihapus", Toast.LENGTH_SHORT).show()
            fetchTasksFromServer()
        }, {
            Toast.makeText(requireContext(), "Gagal hapus task: ${it.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id" to taskId.toString())
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> editText.setText(String.format("%04d-%02d-%02d", y, m + 1, d)) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, h, m -> editText.setText(String.format("%02d:%02d", h, m)) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun insertCategory(categoryName: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val dbHelper = DatabaseHelper(requireContext())
        val success = dbHelper.insertCategory(userId, categoryName)
        if (success) {
            Toast.makeText(requireContext(), "Kategori ditambahkan", Toast.LENGTH_SHORT).show()
            fetchTasksFromServer()
        } else {
            Toast.makeText(requireContext(), "Kategori gagal ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext())
        input.hint = "Nama kategori"

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Kategori")
            .setView(input)
            .setPositiveButton("Tambah") { _, _ ->
                val categoryName = input.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    insertCategory(categoryName)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}