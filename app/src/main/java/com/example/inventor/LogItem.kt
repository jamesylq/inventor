package com.example.inventor

data class LogEntry(
    val timestamp: Long = -1,
    val timeString: String = "unknown",
    val user: String = "unknown",
    val action: Int = -1,
    val details: String = "unknown"
)
