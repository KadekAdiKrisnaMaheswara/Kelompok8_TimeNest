package com.kelompok8.timenest.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.kelompok8.timenest.model.Task
import org.json.JSONArray

class HomeFragment : Fragment() {

    private lateinit var containerTaskList: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        containerTaskList = rootView.findViewById(R.id.containerTasks)
        fetchTasksFromServer()

        return rootView
    }

    private fun fetchTasksFromServer() {
        val url = "http://10.0.2.2/timenest_api/get_task.php"

        val request = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)
                    containerTaskList.removeAllViews() // Clear dulu
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val title = obj.getString("title")
                        val date = obj.getString("end_date")
                        val start = obj.getString("start_time")
                        val end = obj.getString("end_time")

                        val taskView = TextView(requireContext())
                        taskView.text = "$title\n$date | $start - $end"
                        taskView.setPadding(16, 16, 16, 16)
                        taskView.setBackgroundResource(R.drawable.rounded_black_button)
                        taskView.setTextColor(resources.getColor(android.R.color.white))
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, 24)
                        taskView.layoutParams = params

                        containerTaskList.addView(taskView)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
