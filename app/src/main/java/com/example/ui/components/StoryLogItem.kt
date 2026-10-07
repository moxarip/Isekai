package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TurnLog
import com.example.ui.theme.*

@Composable
fun StoryLogItem(
    turnLog: TurnLog,
    modifier: Modifier = Modifier
) {
    val isPrologue = turnLog.turnNumber == 1

    Surface(
        color = AlleyCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrologue) AmberGold.copy(alpha = 0.5f) else AlleyCardBorder
        ),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Turn Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPrologue) AmberGold.copy(alpha = 0.2f) else Color(0x3038BDF8))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isPrologue) "المستهل • البداية" else "الدور #${turnLog.turnNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isPrologue) AmberGold else FrostCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    if (turnLog.actionType != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(turnLog.actionType.badgeColorHex).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = turnLog.actionType.titleAr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(turnLog.actionType.badgeColorHex),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Player's Chosen Decision
            if (!isPrologue && turnLog.playerActionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1522))
                        .border(1.dp, Color(0xFF1E283C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "قرارك",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = turnLog.playerActionText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Atmospheric Narrative (Literary Amiri text)
            Text(
                text = turnLog.narrativeAr,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    lineHeight = 27.sp
                )
            )

            // NPC Dialogue Callout
            if (turnLog.npcDialogue != null) {
                Spacer(modifier = Modifier.height(10.dp))
                NpcDialogueCard(dialogue = turnLog.npcDialogue)
            }

            // Theorem Eureka Card (if unlocked this turn)
            if (turnLog.theoremUnlocked != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF181530),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScienceIndigo.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "اكتشاف فيزيائي",
                                tint = ScienceIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إلهام فيزيائي جديد: ${turnLog.theoremUnlocked.titleAr}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = ScienceIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "القانون: ${turnLog.theoremUnlocked.physicsLawAr}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تطبيق المانا: ${turnLog.theoremUnlocked.manaApplicationAr}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGold)
                        )
                    }
                }
            }
        }
    }
}
