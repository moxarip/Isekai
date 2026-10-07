package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameSessionEntity
import com.example.data.model.CharacterVoiceType
import com.example.ui.theme.*

@Composable
fun HeaderHud(
    session: GameSessionEntity?,
    isAiActive: Boolean,
    isHudExpanded: Boolean,
    isAutoTtsEnabled: Boolean,
    isSpeaking: Boolean,
    currentSpeaker: CharacterVoiceType?,
    isImageGenerationEnabled: Boolean,
    inventoryCount: Int,
    theoremsCount: Int,
    onToggleExpand: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleImages: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenTheorems: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val energy = session?.energy ?: 25
    val hunger = session?.hunger ?: 75
    val warmth = session?.warmth ?: 20
    val suspicion = session?.suspicion ?: 0
    val childMask = session?.childMask ?: 100
    val copper = session?.copperCoins ?: 0
    val mana = session?.manaComprehension ?: 10

    // Suspicion high alert pulse (>60%)
    val infiniteTransition = rememberInfiniteTransition(label = "suspicion_pulse")
    val suspicionAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (suspicion > 60) 0.95f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (suspicion > 60) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "suspicion_glow"
    )

    Surface(
        color = AlleySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AlleyCardBorder),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Main Compact Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title & Chapter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onToggleExpand() }
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(AmberDark, AmberGold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "شعار اللعبة",
                            tint = AlleyBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "إعادة ولادة الشارع",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isHudExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "توسيع أو طي",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isSpeaking && currentSpeaker != null) "يتحدث الآن: ${currentSpeaker.labelAr}" else "زقاق الصقيع • إلدوريا",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSpeaking) VitalEnergy else TextSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Toolbar Actions: Voice, Images, Grimoire, Bag, Key
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Voice TTS Toggle
                    IconButton(
                        onClick = onToggleTts,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_tts_button")
                    ) {
                        Icon(
                            imageVector = if (isAutoTtsEnabled) {
                                if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeDown
                            } else {
                                Icons.Default.VolumeOff
                            },
                            contentDescription = "قراءة صوتية",
                            tint = if (isSpeaking) VitalEnergy else if (isAutoTtsEnabled) TextGold else TextMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Scene Images Toggle
                    IconButton(
                        onClick = onToggleImages,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_images_button")
                    ) {
                        Icon(
                            imageVector = if (isImageGenerationEnabled) Icons.Default.Image else Icons.Default.HideImage,
                            contentDescription = "توليد صور المشاهد",
                            tint = if (isImageGenerationEnabled) FrostCyan else TextMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Theorems / Grimoire
                    IconButton(
                        onClick = onOpenTheorems,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("theorems_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (theoremsCount > 0) {
                                    Badge(containerColor = ScienceIndigo, contentColor = Color.White) {
                                        Text("$theoremsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "قوانين الفيزياء والسحر",
                                tint = ScienceIndigo,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Inventory Button
                    IconButton(
                        onClick = onOpenInventory,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("inventory_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (inventoryCount > 0) {
                                    Badge(containerColor = AmberGold, contentColor = AlleyBackground) {
                                        Text("$inventoryCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkOutline,
                                contentDescription = "الحقيبة",
                                tint = AmberGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("api_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "الإعدادات",
                            tint = if (isAiActive) VitalEnergy else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compact Inline Vital Pills Row (Uncluttered)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Energy Pill
                    CompactPill(
                        icon = Icons.Default.Bolt,
                        value = "$energy%",
                        color = VitalEnergy
                    )
                    // Warmth Pill
                    CompactPill(
                        icon = Icons.Default.AcUnit,
                        value = "$warmth%",
                        color = VitalWarmth
                    )
                    // Suspicion Pill (Pulses if >60%)
                    CompactPill(
                        icon = Icons.Default.Visibility,
                        value = "$suspicion%",
                        color = if (suspicion > 60) SuspicionCritical else SuspicionPurple,
                        glow = suspicion > 60,
                        glowAlpha = suspicionAlpha
                    )
                }

                // Copper Coins & Toggle Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompactPill(
                        icon = Icons.Default.MonetizationOn,
                        value = "$copper",
                        color = AmberGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHudExpanded) "إخفاء التفاصيل" else "التفاصيل",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .clickable { onToggleExpand() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Animated Expanded Details Drawer
            AnimatedVisibility(
                visible = isHudExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // AI Status indicator pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isAiActive) Color(0x2010B981) else Color(0x20F59E0B))
                            .border(
                                1.dp,
                                if (isAiActive) Color(0x6010B981) else Color(0x60F59E0B),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isAiActive) VitalEnergy else AmberGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAiActive) "Gemini Flash مفعّل • توليد نصوص وصور غير محدودة" else "الراوي المحلي • اضغط هنا لإدخال مفتاح Gemini",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isAiActive) VitalEnergy else AmberGold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vital Bars Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailedStatBar(
                            label = "الطاقة",
                            value = energy,
                            color = VitalEnergy,
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.weight(1f)
                        )
                        DetailedStatBar(
                            label = "الشبع",
                            value = (100 - hunger).coerceIn(0, 100),
                            color = VitalHunger,
                            icon = Icons.Default.Restaurant,
                            modifier = Modifier.weight(1f)
                        )
                        DetailedStatBar(
                            label = "الدفء",
                            value = warmth,
                            color = VitalWarmth,
                            icon = Icons.Default.AcUnit,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Metrics (Child mask, Mana)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailedStatBar(
                            label = "قناع البراءة",
                            value = childMask,
                            color = ChildMaskBlue,
                            icon = Icons.Default.Face,
                            modifier = Modifier.weight(1f)
                        )
                        DetailedStatBar(
                            label = "فهم المانا بالفيزياء",
                            value = mana,
                            color = ScienceIndigo,
                            icon = Icons.Default.Science,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    color: Color,
    glow: Boolean = false,
    glowAlpha: Float = 1f
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (glow) color.copy(alpha = glowAlpha * 0.25f) else AlleyCard)
            .border(
                1.dp,
                if (glow) color.copy(alpha = glowAlpha) else AlleyCardBorder,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun DetailedStatBar(
    label: String,
    value: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AlleyCard)
            .border(1.dp, AlleyCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                )
            }
            Text(
                text = "$value/100",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = AlleySurface
        )
    }
}
