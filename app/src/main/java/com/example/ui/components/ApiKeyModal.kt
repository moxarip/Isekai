package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.gemini.GeminiApiClient
import com.example.ui.theme.*

@Composable
fun ApiKeyModal(
    currentMaskedKey: String,
    currentModel: String,
    onSaveKey: (newKey: String, model: String) -> Unit,
    onResetGame: () -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(currentModel) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = AlleySurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AlleyCardBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "مفتاح API",
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إعدادات Gemini AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "عند إدخال مفتاح الـ API من Google AI Studio، يتحول السرد إلى مغامرة مفتوحة لا نهائية تُحلل كل قرار تكتبه بدقة واقعية!",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (currentMaskedKey.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2010B981))
                            .border(1.dp, Color(0x6010B981), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "نشط",
                            tint = VitalEnergy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المفتاح الحالي مفعل: $currentMaskedKey",
                            style = MaterialTheme.typography.labelSmall.copy(color = VitalEnergy)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // API Key Text Field
                Text(
                    text = "مفتاح Google AI Studio API:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextGold, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = {
                        Text(
                            text = if (currentMaskedKey.isNotBlank()) "أدخل مفتاحاً جديداً لتغييره..." else "AIzaSy...",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "إظهار المفتاح",
                                tint = TextMuted
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AlleyCard,
                        unfocusedContainerColor = AlleyCard,
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = AlleyCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Model Selector
                Text(
                    text = "نموذج الذكاء الاصطناعي (Model):",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextGold, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val models = listOf(
                        GeminiApiClient.MODEL_FLASH_LATEST to "Gemini Flash (أسرع وأذكى)",
                        GeminiApiClient.MODEL_35_FLASH to "Gemini 3.5 Flash"
                    )
                    models.forEach { (modelId, modelLabel) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            color = if (isSelected) AmberDark.copy(alpha = 0.25f) else AlleyCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberGold else AlleyCardBorder
                            ),
                            onClick = { selectedModel = modelId },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modelLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) AmberGold else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onResetGame()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SuspicionCritical)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "إعادة اللعبة", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة بدء اللعبة", style = MaterialTheme.typography.labelSmall)
                    }

                    Row {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveKey(keyInput, selectedModel)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberDark,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Text("حفظ وتفعيل", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
