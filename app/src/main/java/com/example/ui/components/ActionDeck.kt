package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.SuggestedAction
import com.example.ui.theme.*

@Composable
fun ActionDeck(
    actions: List<SuggestedAction>,
    isLoading: Boolean,
    onSelectAction: (SuggestedAction) -> Unit,
    onSubmitCustomText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customText by remember { mutableStateOf("") }

    Surface(
        color = AlleySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AlleyCardBorder),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Header: Section label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "الخيارات التكتيكية",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خياراتك التكتيكية:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = AmberGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "العقل الاستراتيجي يحلل الموقف...",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                } else {
                    Text(
                        text = "أو اكتب قرارك الحر بالأسفل",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Suggested Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                actions.take(3).forEachIndexed { index, action ->
                    val badgeColor = Color(action.actionType.badgeColorHex)
                    val iconVector = when (action.actionType) {
                        ActionType.CHILD_FACADE -> Icons.Default.Face
                        ActionType.STRATEGIC -> Icons.Default.TrendingUp
                        ActionType.DARING -> Icons.Default.DirectionsRun
                        ActionType.SCIENTIFIC -> Icons.Default.Science
                    }

                    Surface(
                        color = AlleyCard,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                        enabled = !isLoading,
                        onClick = { onSelectAction(action) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("action_button_$index")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = action.actionType.titleAr,
                                    tint = badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = action.labelAr,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                if (action.subtitleAr.isNotBlank()) {
                                    Text(
                                        text = action.subtitleAr,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "تنفيذ",
                                tint = badgeColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Free Custom Input Field with Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = {
                        Text(
                            text = "أدخل قرارك الحر هنا... (اكتب ما تريد أن يفعله ليو)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (customText.isNotBlank()) {
                                onSubmitCustomText(customText.trim())
                                customText = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AlleyCard,
                        unfocusedContainerColor = AlleyCard,
                        disabledContainerColor = AlleyCard.copy(alpha = 0.5f),
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = AlleyCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("custom_action_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (customText.isNotBlank()) {
                            onSubmitCustomText(customText.trim())
                            customText = ""
                        }
                    },
                    enabled = !isLoading && customText.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberDark,
                        contentColor = Color.White,
                        disabledContainerColor = AlleyCardBorder,
                        disabledContentColor = TextMuted
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("submit_custom_action_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "تنفيذ القرار",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
