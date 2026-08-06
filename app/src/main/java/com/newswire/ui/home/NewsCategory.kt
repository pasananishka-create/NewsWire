package com.newswire.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SportsCricket
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.ui.graphics.vector.ImageVector

enum class NewsCategory(
    val query: String,
    val title: String,
    val icon: ImageVector,
) {
    ALL("sri lanka", "All", Icons.Rounded.Public),
    CRICKET("sri lanka cricket", "Cricket", Icons.Rounded.SportsCricket),
    POLITICS("sri lanka politics", "Politics", Icons.Rounded.AccountBalance),
    BUSINESS("sri lanka economy", "Business", Icons.AutoMirrored.Rounded.TrendingUp),
    WEATHER("sri lanka weather", "Weather", Icons.Rounded.Thunderstorm),
    ENTERTAINMENT("sri lanka entertainment", "Entertainment", Icons.Rounded.Movie),
}
