package com.kelompok8.timenest.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelompok8.timenest.R
import com.kelompok8.timenest.data.TaskData

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerTasks)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = TaskAdapter(TaskData.taskList)

        return view
    }
}
