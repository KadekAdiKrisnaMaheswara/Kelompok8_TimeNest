package com.kelompok8.timenest.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.kelompok8.timenest.model.GroupedTask
import com.kelompok8.timenest.model.Task
import com.kelompok8.timenest.ui.GroupedCategoryAdapter
import org.json.JSONArray

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

        // Ambil nama user
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", null)
        val greetingName = if (!name.isNullOrBlank()) name else getString(R.string.default_user_name)
        tvWelcome.text = getString(R.string.greeting, greetingName)

        recyclerCategories = rootView.findViewById(R.id.recyclerCategories)
        recyclerCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        groupedCategoryAdapter = GroupedCategoryAdapter(
            groupedList,
            onCategoryClick = { selectedTasks ->
                updateTaskList(selectedTasks)
            }
        )
        recyclerCategories.adapter = groupedCategoryAdapter

        recyclerTasks = rootView.findViewById(R.id.recyclerViewTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(
            mutableListOf(),
            onEditClick = { task ->
                showEditTaskDialog(task)
            },
            onDeleteClick = { task ->
                confirmDeleteTask(task)
            }
        )
        recyclerTasks.adapter = taskAdapter

        fetchTasksFromServer()
        setupSearchListener()

        return rootView
    }

    private fun fetchTasksFromServer() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) return

        val url = "http://10.0.2.2/timenest_api/get_task.php?user_id=$userId"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val taskMap = mutableMapOf<String, MutableList<Task>>()
                    fullTaskList.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val categoryName = obj.optString("category")
                        val task = Task(
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            category = if (categoryName.isNullOrBlank() || categoryName == "null") "Uncategorized" else categoryName,
                            endDate = obj.getString("end_date"),
                            startTime = obj.getString("start_time"),
                            endTime = obj.getString("end_time"),
                            remind = obj.getString("remind")
                        )
                        fullTaskList.add(task)

                        val key = task.category
                        taskMap.getOrPut(key) { mutableListOf() }.add(task)
                    }

                    groupedList.clear()
                    for ((category, tasks) in taskMap) {
                        groupedList.add(GroupedTask(category, tasks))
                    }

                    groupedCategoryAdapter.notifyDataSetChanged()
                    updateTaskList(fullTaskList)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Gagal parsing task", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
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
        if (query.isEmpty()) {
            updateTaskList(fullTaskList)
            return
        }

        val filtered = fullTaskList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
        updateTaskList(filtered)
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_task, null)

        val edtTitle = dialogView.findViewById<EditText>(R.id.editTaskTitle)
        val edtEndDate = dialogView.findViewById<EditText>(R.id.editTaskEndDate)
        val edtStartTime = dialogView.findViewById<EditText>(R.id.editTaskStartTime)
        val edtEndTime = dialogView.findViewById<EditText>(R.id.editTaskEndTime)
        val edtRemind = dialogView.findViewById<EditText>(R.id.editTaskRemind)

        edtTitle.setText(task.title)
        edtEndDate.setText(task.endDate)
        edtStartTime.setText(task.startTime)
        edtEndTime.setText(task.endTime)
        edtRemind.setText(task.remind)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newTitle = edtTitle.text.toString().trim()
                val newEndDate = edtEndDate.text.toString().trim()
                val newStartTime = edtStartTime.text.toString().trim()
                val newEndTime = edtEndTime.text.toString().trim()
                val newRemind = edtRemind.text.toString().trim()

                updateTask(
                    task.id,
                    newTitle,
                    newEndDate,
                    newStartTime,
                    newEndTime,
                    newRemind
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateTask(
        taskId: Int,
        title: String,
        endDate: String,
        startTime: String,
        endTime: String,
        remind: String
    ) {
        val url = "http://10.0.2.2/timenest_api/update_task.php"

        val request = object : StringRequest(Method.POST, url,
            {
                Toast.makeText(requireContext(), "Task berhasil diupdate", Toast.LENGTH_SHORT).show()
                fetchTasksFromServer()
            },
            {
                Toast.makeText(requireContext(), "Gagal update task: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id" to taskId.toString(),
                    "title" to title,
                    "end_date" to endDate,
                    "start_time" to startTime,
                    "end_time" to endTime,
                    "remind" to remind
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun confirmDeleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Task")
            .setMessage("Apakah yakin ingin menghapus task '${task.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteTask(task.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTask(taskId: Int) {
        val url = "http://10.0.2.2/timenest_api/delete_task.php"

        val request = object : StringRequest(Method.POST, url,
            {
                Toast.makeText(requireContext(), "Task berhasil dihapus", Toast.LENGTH_SHORT).show()
                fetchTasksFromServer()
            },
            {
                Toast.makeText(requireContext(), "Gagal hapus task: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id" to taskId.toString())
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
