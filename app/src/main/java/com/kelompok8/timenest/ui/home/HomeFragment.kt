package com.kelompok8.timenest.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val groupedList = mutableListOf<GroupedTask>()

    private lateinit var groupedCategoryAdapter: GroupedCategoryAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Setup recycler untuk kategori
        recyclerCategories = rootView.findViewById(R.id.recyclerCategories)
        recyclerCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        groupedCategoryAdapter = GroupedCategoryAdapter(groupedList) { selectedTasks ->
            updateTaskList(selectedTasks)
        }
        recyclerCategories.adapter = groupedCategoryAdapter

        // Setup recycler untuk daftar task di bawah
        recyclerTasks = rootView.findViewById(R.id.recyclerViewTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(mutableListOf())
        recyclerTasks.adapter = taskAdapter

        // Ambil data dari server
        fetchTasksFromServer()

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

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val task = Task(
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            category = obj.optString("category") ?: "Uncategorized",
                            endDate = obj.getString("end_date"),
                            startTime = obj.getString("start_time"),
                            endTime = obj.getString("end_time"),
                            remind = obj.getString("remind")
                        )

                        val key = task.category.ifBlank { "Uncategorized" }
                        taskMap.getOrPut(key) { mutableListOf() }.add(task)
                    }

                    groupedList.clear()
                    groupedList.addAll(taskMap.map { (category, tasks) ->
                        GroupedTask(category, tasks)
                    })
                    groupedCategoryAdapter.notifyDataSetChanged()

                    // ✅ Tampilkan semua task langsung di bawah
                    val allTasks = groupedList.flatMap { it.tasks }
                    updateTaskList(allTasks)

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
}
