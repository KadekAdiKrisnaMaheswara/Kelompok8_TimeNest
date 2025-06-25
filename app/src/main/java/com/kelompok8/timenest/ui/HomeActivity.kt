package com.kelompok8.timenest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok8.timenest.databinding.ActivityHomeBinding
import com.kelompok8.timenest.data.DatabaseHelper
import com.kelompok8.timenest.ui.home.CategoryAdapter
import com.kelompok8.timenest.ui.home.TaskAdapter

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DatabaseHelper(this)
        val categories = dbHelper.getAllCategories()
        val tasks = dbHelper.getOngoingTasks()

        categoryAdapter = CategoryAdapter(categories)
        taskAdapter = TaskAdapter(tasks)

        binding.recyclerCategories.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategories.adapter = categoryAdapter

        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = taskAdapter
    }
}