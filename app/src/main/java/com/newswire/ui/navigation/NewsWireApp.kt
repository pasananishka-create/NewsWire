package com.newswire.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.newswire.ui.home.HomeScreen
import com.newswire.ui.reader.ArticleReaderScreen

@Composable
fun NewsWireApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
    ) {
        composable(route = "home") {
            HomeScreen(
                onArticleClick = { article ->
                    navController.navigate(
                        "reader?url=${Uri.encode(article.link)}&title=${Uri.encode(article.title)}"
                    )
                },
            )
        }
        composable(
            route = "reader?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
            enterTransition = {
                slideInVertically(animationSpec = tween(320)) { it } + fadeIn(tween(300))
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = {
                slideOutVertically(animationSpec = tween(280)) { it } + fadeOut(tween(240))
            },
        ) { entry ->
            val url = Uri.decode(entry.arguments?.getString("url").orEmpty())
            val title = Uri.decode(entry.arguments?.getString("title").orEmpty())
            ArticleReaderScreen(
                url = url,
                title = title,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
