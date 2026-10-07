package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.DiscoveredTheorem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoremsSheet(
    theorems: List<DiscoveredTheorem>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AlleySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AlleyCardBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "قوانين الفيزياء والسحر",
                        tint = ScienceIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دفتر الفيزياء السحرية (عقل الأربعين)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Text(
                    text = "${theorems.size} قوانين مكتشفة",
                    style = MaterialTheme.typography.labelSmall.copy(color = ScienceIndigo)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "السحر في إلدوريا ليس طقوساً دينية للأرستقراطيين، بل فيزياء وميكانيكا طاقة بحتة يفهمها ليو بدقة ويتفوق بها على سحرة الأكاديمية.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (theorems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لم تكتشف قوانين جديدة بعد...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                        Text(
                            text = "اختر قرارات [الفيزياء والسحر] في المواقف القادمة لاستنباط القوانين.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(theorems) { th ->
                        Surface(
                            color = AlleyCard,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ScienceIndigo.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = th.titleAr,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ScienceIndigo
                                        )
                                    )
                                    Text(
                                        text = "الدور #${th.discoveryTurn}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الأصل الفيزيائي الأرضي:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = th.physicsLawAr,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "التطبيق السحري في إلدوريا:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FrostCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = th.manaApplicationAr,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
