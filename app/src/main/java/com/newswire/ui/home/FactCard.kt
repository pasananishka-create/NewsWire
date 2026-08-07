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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Lightbulb
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newswire.data.model.FunFact

private val FactGradients = listOf(
    listOf(Color(0xFF2A3B7C), Color(0xFF070B1A)),
    listOf(Color(0xFF1F4E79), Color(0xFF07101A)),
    listOf(Color(0xFF3C1E6E), Color(0xFF0D0718)),
    listOf(Color(0xFF14532D), Color(0xFF07140B)),
    listOf(Color(0xFF7B2D26), Color(0xFF1A0806)),
    listOf(Color(0xFF123F6E), Color(0xFF070F1A)),
)

private fun factGradient(seed: String): List<Color> =
    FactGradients[Math.floorMod(seed.hashCode(), FactGradients.size)]

@Composable
fun FactCard(
    fact: FunFact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = remember(fact.id) { factGradient(fact.id) }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        label = "factScale",
    )

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(bottom = 48.dp),
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.10f)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FUN FACT",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFFFD166),
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = fact.text.ifBlank { "Did you know…" },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (fact.source.isNotBlank()) {
                Text(
                    text = "source: ${fact.source}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    fontStyle = FontStyle.Italic,
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

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
                    text = "TAP TO READ MORE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}
