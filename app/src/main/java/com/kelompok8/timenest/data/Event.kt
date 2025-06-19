package com.kelompok8.timenest.data

data class Event(
    val id: Int = 0,
    val title: String,
    val date: String,      // Format: "YYYY-MM-DD"
    val time: String,      // Format: "HH:MM"
    val location: String?,
    val category: String   // contoh: "ulang tahun", "rapat", "deadline"
)
