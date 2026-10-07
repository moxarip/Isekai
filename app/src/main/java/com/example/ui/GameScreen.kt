package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SuggestedAction
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll when new turn is added
    LaunchedEffect(uiState.turnLogs.size) {
        if (uiState.turnLogs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(AlleyBackground),
            containerColor = AlleyBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                HeaderHud(
                    session = uiState.session,
                    isAiActive = uiState.isAiActive,
                    isHudExpanded = uiState.isHudExpanded,
                    isAutoTtsEnabled = uiState.isAutoTtsEnabled,
                    isSpeaking = uiState.isSpeaking,
                    currentSpeaker = uiState.currentSpeaker,
                    isImageGenerationEnabled = uiState.isImageGenerationEnabled,
                    inventoryCount = uiState.inventory.size,
                    theoremsCount = uiState.theorems.size,
                    onToggleExpand = { viewModel.toggleHudExpanded() },
                    onToggleTts = { viewModel.toggleAutoTts() },
                    onToggleImages = { viewModel.toggleImageGeneration() },
                    onOpenInventory = { viewModel.toggleInventory(true) },
                    onOpenTheorems = { viewModel.toggleTheorems(true) },
                    onOpenSettings = { viewModel.toggleApiKeyDialog(true) }
                )
            },
            bottomBar = {
                ActionDeck(
                    actions = uiState.currentSuggestedActions,
                    isLoading = uiState.isLoading,
                    onSelectAction = { action: SuggestedAction ->
                        viewModel.submitAction(action.labelAr, action.actionType)
                    },
                    onSubmitCustomText = { text ->
                        viewModel.submitAction(text, null)
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main Story Theater (Scrollable Logs)
                LazyColumn(
                    state = listState,
                    reverseLayout = false,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("story_logs_list")
                ) {
                    items(
                        items = uiState.turnLogs,
                        key = { it.id }
                    ) { log ->
                        StoryLogItem(
                            turnLog = log,
                            isSpeakingThis = uiState.isSpeaking,
                            onPlayAudio = { viewModel.speakTurn(log) }
                        )
                    }
                }

                // Notification / Eureka Banner
                AnimatedVisibility(
                    visible = uiState.bannerMessage != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    Surface(
                        color = Color(0xFF1E283C),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.bannerMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGold, fontSize = 11.sp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inventory Sheet
        if (uiState.showInventorySheet) {
            InventorySheet(
                items = uiState.inventory,
                onDismiss = { viewModel.toggleInventory(false) },
                onUseItemAsAction = { item ->
                    viewModel.submitAction("استخدام [${item.nameAr}] في هذا الموقف", null)
                }
            )
        }

        // Theorems Sheet
        if (uiState.showTheoremsSheet) {
            TheoremsSheet(
                theorems = uiState.theorems,
                onDismiss = { viewModel.toggleTheorems(false) }
            )
        }

        // API Key Settings Modal
        if (uiState.showApiKeyDialog) {
            ApiKeyModal(
                currentMaskedKey = uiState.activeApiKeyMasked,
                currentModel = uiState.selectedModel,
                isAutoTtsEnabled = uiState.isAutoTtsEnabled,
                isImageGenEnabled = uiState.isImageGenerationEnabled,
                onToggleTts = { viewModel.toggleAutoTts() },
                onToggleImageGen = { viewModel.toggleImageGeneration() },
                onSaveKey = { key, model ->
                    viewModel.saveApiKey(key, model)
                },
                onResetGame = {
                    viewModel.resetGame()
                },
                onDismiss = { viewModel.toggleApiKeyDialog(false) }
            )
        }

        // Game Over Dialog
        if (uiState.showGameOverDialog) {
            GameOverDialog(
                reason = uiState.session?.gameOverReason,
                turnCount = uiState.session?.turnCount ?: 1,
                onRestart = {
                    viewModel.resetGame()
                }
            )
        }
    }
}
