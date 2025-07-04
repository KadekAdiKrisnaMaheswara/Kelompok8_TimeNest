package com.kelompok8.timenest.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelompok8.timenest.R
import com.kelompok8.timenest.model.GroupedTask
import com.kelompok8.timenest.ui.home.TaskAdapter

class GroupedTaskAdapter(
    private val groupedTasks: List<GroupedTask>
) : RecyclerView.Adapter<GroupedTaskAdapter.GroupedTaskViewHolder>() {

    inner class GroupedTaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.textCategoryName)
        val taskRecycler: RecyclerView = view.findViewById(R.id.recyclerTasksInCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grouped_task, parent, false)
        return GroupedTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupedTaskViewHolder, position: Int) {
        val grouped = groupedTasks[position]
        holder.categoryName.text = grouped.category

        val taskAdapter = TaskAdapter(grouped.tasks.toMutableList())
        holder.taskRecycler.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.taskRecycler.adapter = taskAdapter
    }

    override fun getItemCount(): Int = groupedTasks.size
}
