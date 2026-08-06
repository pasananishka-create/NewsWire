package com.newswire.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import coil.compose.AsyncImage
import com.newswire.data.model.Article
import kotlinx.coroutines.delay
import java.util.Locale

private val SourcePalette = listOf(
    Color(0xFF6C8CFF),
    Color(0xFF34D399),
    Color(0xFFF0B45A),
    Color(0xFFA78BFA),
    Color(0xFFF472B6),
    Color(0xFF4ADE80),
    Color(0xFFF87171),
    Color(0xFF22D3EE),
    Color(0xFFFB923C),
    Color(0xFF2DD4BF),
)

fun sourceColor(name: String): Color {
    if (name.isBlank()) return SourcePalette.first()
    return SourcePalette[Math.floorMod(name.hashCode(), SourcePalette.size)]
}

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
fun ArticleCard(
    article: Article,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(article.url) {
        delay((index * 70L).coerceAtMost(560L))
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow),
        )
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        label = "pressScale",
    )

    val shape = RoundedCornerShape(26.dp)
    val accent = sourceColor(article.source.name)

    Box(
        modifier = modifier.graphicsLayer {
            val p = entrance.value
            alpha = p
            translationY = (1f - p) * 28f
            scaleX = 0.97f + 0.03f * p
            scaleY = 0.97f + 0.03f * p
        }.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            interactionSource = interaction,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(186.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                ) {
                    if (!article.urlToImage.isNullOrBlank()) {
                        AsyncImage(
                            model = article.urlToImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Article,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.30f))
                                )
                            )
                    )

                    Box(modifier = Modifier.padding(12.dp)) {
                        SourceBadge(name = article.source.name, accent = accent)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = article.source.name.ifBlank { "News" },
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (formatTimeAgo(article.publishedAt).isNotBlank()) {
                            Box(
                                Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            )
                            Text(
                                text = formatTimeAgo(article.publishedAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (!article.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceBadge(name: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B0F19).copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = initials(name).ifBlank { "N" },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF0B0F19),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name.ifBlank { "News" },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF0B0F19),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
