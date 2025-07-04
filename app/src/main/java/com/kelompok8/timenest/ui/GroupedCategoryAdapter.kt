package com.kelompok8.timenest.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok8.timenest.R
import com.kelompok8.timenest.model.GroupedTask
import com.kelompok8.timenest.model.Task

class GroupedCategoryAdapter(
    private val groupedTasks: List<GroupedTask>,
    private val onCategoryClick: (List<Task>) -> Unit
) : RecyclerView.Adapter<GroupedCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_expandable, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val group = groupedTasks[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int = groupedTasks.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvTaskCount)

        fun bind(group: GroupedTask) {
            tvCategory.text = group.category
            tvCount.text = "${group.tasks.size} Tasks"

            itemView.setOnClickListener {
                onCategoryClick(group.tasks)
            }
        }
    }
}
