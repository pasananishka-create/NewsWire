package com.newswire.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Language
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
    ALL("", "All", Icons.Rounded.Public),
    SRI_LANKA("sri lanka", "Sri Lanka", Icons.Rounded.Flag),
    WORLD("world", "World", Icons.Rounded.Language),
    CRICKET("cricket", "Cricket", Icons.Rounded.SportsCricket),
    POLITICS("politics", "Politics", Icons.Rounded.AccountBalance),
    BUSINESS("business", "Business", Icons.AutoMirrored.Rounded.TrendingUp),
    TECHNOLOGY("technology", "Tech", Icons.Rounded.Devices),
    ENTERTAINMENT("entertainment", "Entertainment", Icons.Rounded.Movie),
    WEATHER("weather", "Weather", Icons.Rounded.Thunderstorm),
}
