package com.kelompok8.timenest.data

data class Reminder(
    val id: Int = 0,
    val targetId: Int,         // bisa id dari task atau event
    val type: String,          // "task" atau "event"
    val remindAt: String       // Format: "YYYY-MM-DD HH:MM"
)