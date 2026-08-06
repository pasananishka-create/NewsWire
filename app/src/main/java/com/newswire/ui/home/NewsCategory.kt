package com.newswire.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.ui.graphics.vector.ImageVector

enum class NewsCategory(
    val api: String,
    val title: String,
    val icon: ImageVector,
) {
    TOP("general", "Top", Icons.Rounded.Public),
    TECHNOLOGY("technology", "Tech", Icons.Rounded.Devices),
    BUSINESS("business", "Business", Icons.AutoMirrored.Rounded.TrendingUp),
    ENTERTAINMENT("entertainment", "Entertainment", Icons.Rounded.Movie),
    HEALTH("health", "Health", Icons.Rounded.Favorite),
    SCIENCE("science", "Science", Icons.Rounded.Science),
}
