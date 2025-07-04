package com.kelompok8.timenest.model

data class GroupedTask(
    val category: String,
    val tasks: List<Task>,
    var isExpanded: Boolean = false
)




