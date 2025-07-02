package com.kelompok8.timenest.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.kelompok8.timenest.model.Task
import org.json.JSONArray

class HomeFragment : Fragment() {

    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerTasks: RecyclerView
    private val categoryList = mutableListOf<String>()
    private val taskList = mutableListOf<Task>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Setup recycler categories
        recyclerCategories = rootView.findViewById(R.id.recyclerCategories)
        recyclerCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter(categoryList)
        recyclerCategories.adapter = categoryAdapter

        // Setup recycler tasks
        recyclerTasks = rootView.findViewById(R.id.recyclerViewTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(taskList)
        recyclerTasks.adapter = taskAdapter

        // Fetch data
        fetchCategoriesFromServer()
        fetchTasksFromServer()

        return rootView
    }

    private fun fetchCategoriesFromServer() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        val url = "http://10.0.2.2/timenest_api/get_categories.php?user_id=$userId"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    categoryList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val category = obj.getString("name")
                        categoryList.add(category)
                    }
                    categoryAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Parse kategori gagal", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchTasksFromServer() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        val url = "http://10.0.2.2/timenest_api/get_task.php?user_id=$userId"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    taskList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val task = Task(
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            category = obj.optString("category", "Unknown"),
                            endDate = obj.getString("end_date"),
                            startTime = obj.getString("start_time"),
                            endTime = obj.getString("end_time"),
                            remind = obj.getString("remind")
                        )
                        taskList.add(task)
                    }
                    taskAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Parse error", e)
                    Toast.makeText(requireContext(), "Gagal parsing data task", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
