package com.newswire.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.newswire.data.model.Article
import com.newswire.ui.components.formatTimeAgo
import java.util.Locale

private val DeckGradients = listOf(
    listOf(Color(0xFF5B7CFA), Color(0xFF101735)),
    listOf(Color(0xFF0FA878), Color(0xFF072D1E)),
    listOf(Color(0xFFE8A33D), Color(0xFF39290D)),
    listOf(Color(0xFF9B59E8), Color(0xFF23103F)),
    listOf(Color(0xFFE0558F), Color(0xFF3A0D21)),
    listOf(Color(0xFF16A6C9), Color(0xFF0A232F)),
    listOf(Color(0xFFF0793E), Color(0xFF3A1A0A)),
    listOf(Color(0xFF4ADE80), Color(0xFF0E3320)),
    listOf(Color(0xFFEF4444), Color(0xFF3B0E0E)),
    listOf(Color(0xFF8B5CF6), Color(0xFF1E1240)),
)

private fun deckGradient(seed: String): List<Color> =
    DeckGradients[Math.floorMod(seed.hashCode(), DeckGradients.size)]

private fun initials(name: String): String {
    val clean = name.trim().replace("News", "").trim()
    val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "N"
        words.size == 1 -> words[0].take(1)
        else -> words[0].take(1) + words[1].take(1)
    }.uppercase(Locale.ROOT)
}

@Composable
fun StoryCard(
    article: Article,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = remember(article.title) { deckGradient(article.title + article.source) }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        label = "storyScale",
    )

    val seed = remember(article.title) { Math.floorMod(article.title.hashCode(), 100) }
    val circleA = CircleSpec(size = 260, offsetX = -70 + (seed % 60), offsetY = 90 + (seed % 120))
    val circleB = CircleSpec(size = 340, offsetX = 170 + (seed % 70), offsetY = 40 + (seed % 80))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
    ) {
        if (imageUrl.isNullOrBlank()) {
            DecorativeCircle(
                spec = circleA,
                color = Color.White.copy(alpha = 0.05f),
            )
            DecorativeCircle(
                spec = circleB,
                color = Color.White.copy(alpha = 0.04f),
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(430.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials(article.source).ifBlank { "N" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = article.source.ifBlank { "News" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (formatTimeAgo(article.publishedAt).isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeAgo(article.publishedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )

            if (article.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "TAP TO READ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

private data class CircleSpec(
    val size: Int,
    val offsetX: Int,
    val offsetY: Int,
)

@Composable
private fun DecorativeCircle(spec: CircleSpec, color: Color) {
    Box(
        modifier = Modifier
            .offset(x = spec.offsetX.dp, y = spec.offsetY.dp)
            .size(spec.size.dp)
            .clip(CircleShape)
            .background(color)
    )
}
