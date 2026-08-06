package com.newswire.ui.components

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun formatTimeAgo(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val time = Instant.parse(iso)
        val now = Instant.now()
        val seconds = Duration.between(time, now).seconds
        when {
            seconds < 0 -> "Now"
            seconds < 60 -> "Now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            seconds < 604800 -> "${seconds / 86400}d ago"
            else -> {
                val date = LocalDateTime.ofInstant(time, ZoneId.systemDefault())
                "${date.dayOfMonth} ${date.month.name.take(3).lowercase()}"
            }
        }
    } catch (_: Exception) {
        ""
    }
}
