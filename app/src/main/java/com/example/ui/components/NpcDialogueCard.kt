package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NpcDialogue
import com.example.data.model.NpcTone
import com.example.ui.theme.*

@Composable
fun NpcDialogueCard(
    dialogue: NpcDialogue?,
    modifier: Modifier = Modifier
) {
    if (dialogue == null || dialogue.speechAr.isBlank()) return

    val toneColor = Color(dialogue.tone.colorHex)
    val speakerIcon = when {
        dialogue.speakerName.contains("روجر") -> Icons.Default.SportsMartialArts
        dialogue.speakerName.contains("مورفاث") || dialogue.speakerName.contains("ساحر") -> Icons.Default.AutoFixHigh
        dialogue.speakerName.contains("حارس") || dialogue.speakerName.contains("فارس") -> Icons.Default.Shield
        dialogue.speakerName.contains("خباز") || dialogue.speakerName.contains("تاجر") -> Icons.Default.Storefront
        else -> Icons.Default.Person
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 },
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            color = Color(0xFF131924),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, toneColor.copy(alpha = 0.6f)),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header with Speaker & Tone Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(toneColor.copy(alpha = 0.2f))
                                .border(1.dp, toneColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = speakerIcon,
                                contentDescription = dialogue.speakerName,
                                tint = toneColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dialogue.speakerName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    // Tone Badge
                    Surface(
                        color = toneColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, toneColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = dialogue.tone.labelAr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = toneColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dialogue Quote
                Text(
                    text = "«${dialogue.speechAr}»",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGold,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C1018))
                        .padding(10.dp)
                )
            }
        }
    }
}
