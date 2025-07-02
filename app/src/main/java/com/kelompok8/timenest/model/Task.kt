package com.kelompok8.timenest.model

data class Task(
    val id: Int,
    val title: String,
    val category: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val remind: String,
    val isCompleted: Boolean = false // ← default value
)
