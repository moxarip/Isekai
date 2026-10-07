package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import com.example.ui.theme.*

@Composable
fun HeaderHud(
    session: GameSessionEntity?,
    isAiActive: Boolean,
    inventoryCount: Int,
    theoremsCount: Int,
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
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top App Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Title & Chapter
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(AmberDark, AmberGold)))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "شعار اللعبة",
                            tint = AlleyBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "إعادة ولادة الشارع",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "عالم إلدوريا • الفصل الأول: زقاق الصقيع",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                    }
                }

                // Action Buttons (Grimoire, Bag, Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Grimoire / Theorems button
                    IconButton(
                        onClick = onOpenTheorems,
                        modifier = Modifier
                            .size(38.dp)
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
                                contentDescription = "قوانين الفيزياء والسحر المكتشفة",
                                tint = ScienceIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Inventory Button
                    IconButton(
                        onClick = onOpenInventory,
                        modifier = Modifier
                            .size(38.dp)
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
                                contentDescription = "الحقيبة والمقتنيات",
                                tint = AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Settings / API Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("api_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "إعدادات Gemini API",
                            tint = if (isAiActive) VitalEnergy else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Status indicator pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isAiActive) Color(0x2010B981) else Color(0x20F59E0B))
                    .border(
                        1.dp,
                        if (isAiActive) Color(0x6010B981) else Color(0x60F59E0B),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onOpenSettings() }
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAiActive) VitalEnergy else AmberGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAiActive) "متصل بـ Gemini Flash (توليد غير محدود)" else "وضع الراوي المحلي (اضغط لتفعيل API)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isAiActive) VitalEnergy else AmberGold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vital Stats Progress Gauges Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Energy (الطاقة)
                StatBarItem(
                    label = "الطاقة",
                    value = energy,
                    maxValue = 100,
                    color = VitalEnergy,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )

                // Hunger (الجوع)
                StatBarItem(
                    label = "الشبع",
                    value = (100 - hunger).coerceIn(0, 100), // Inverted for satiation
                    maxValue = 100,
                    color = VitalHunger,
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f)
                )

                // Warmth (الدفء)
                StatBarItem(
                    label = "الدفء",
                    value = warmth,
                    maxValue = 100,
                    color = VitalWarmth,
                    icon = Icons.Default.AcUnit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Suspicion Meter with ominous pulse
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AlleyCard)
                    .border(
                        1.dp,
                        if (suspicion > 60) SuspicionCritical.copy(alpha = suspicionAlpha) else AlleyCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "مقياس الشك",
                            tint = if (suspicion > 60) SuspicionCritical else SuspicionPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (suspicion > 75) "مقياس الشك (خطر إعدام الاستحواذ!)" else "مقياس الشك والاشتباه",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (suspicion > 60) SuspicionCritical else SuspicionPurple,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "$suspicion%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (suspicion > 60) SuspicionCritical else SuspicionPurple,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { (suspicion / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (suspicion > 75) SuspicionCritical else SuspicionPurple,
                    trackColor = Color(0xFF1E1528)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Metrics (Child Mask, Coins, Science Mana)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Child Mask
                CompactBadge(
                    icon = Icons.Default.Face,
                    label = "قناع البراءة",
                    value = "$childMask%",
                    color = ChildMaskBlue,
                    modifier = Modifier.weight(1f)
                )

                // Copper Coins
                CompactBadge(
                    icon = Icons.Default.MonetizationOn,
                    label = "نحاس",
                    value = "$copper",
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )

                // Mana Comprehension
                CompactBadge(
                    icon = Icons.Default.Science,
                    label = "فهم المانا",
                    value = "$mana",
                    color = ScienceIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBarItem(
    label: String,
    value: Int,
    maxValue: Int,
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
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                )
            }
            Text(
                text = "$value/$maxValue",
                style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / maxValue.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = AlleySurface
        )
    }
}

@Composable
private fun CompactBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AlleyCard)
            .border(1.dp, AlleyCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
        }
    }
}
