package com.kelompok8.timenest.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok8.timenest.R
import com.kelompok8.timenest.model.Task

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size

    fun updateData(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        private val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        private val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        private val tvTaskTime: TextView = itemView.findViewById(R.id.tvTaskTime)
        private val tvTaskRemind: TextView = itemView.findViewById(R.id.tvTaskRemind)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_task)

        fun bind(task: Task) {
            tvTaskTitle.text = task.title
            tvTaskCategory.text = "Category: ${task.categories}"
            tvTaskDate.text = "End Date: ${task.endDate}"
            tvTaskTime.text = "Time: ${task.startTime} - ${task.endTime}"
            tvTaskRemind.text = "Remind: ${task.remind}"

            ivEdit.setOnClickListener {
                onEditClick(task)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }
}
