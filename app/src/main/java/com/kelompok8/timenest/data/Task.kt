package com.kelompok8.timenest.data

data class Task(
    val id: Int = 0,
    val name: String,
    val description: String,
    val deadline: String,  // Format: "YYYY-MM-DD HH:MM"
    val isDone: Boolean,
    val priority: Int      // 1 = tinggi, 2 = sedang, 3 = rendah (misalnya)
)