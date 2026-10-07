package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CharacterVoiceManager
import com.example.data.db.GameSessionEntity
import com.example.data.gemini.GeminiApiClient
import com.example.data.model.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameUiState(
    val session: GameSessionEntity? = null,
    val inventory: List<InventoryItem> = emptyList(),
    val turnLogs: List<TurnLog> = emptyList(),
    val theorems: List<DiscoveredTheorem> = emptyList(),
    val currentSuggestedActions: List<SuggestedAction> = defaultInitialActions(),
    val isLoading: Boolean = false,
    val isAiActive: Boolean = false,
    val selectedModel: String = GeminiApiClient.MODEL_FLASH_LATEST,
    val activeApiKeyMasked: String = "",
    val showInventorySheet: Boolean = false,
    val showTheoremsSheet: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val bannerMessage: String? = null,
    // Decluttered UI & Audio/Image options
    val isHudExpanded: Boolean = false,
    val isAutoTtsEnabled: Boolean = true,
    val isSpeaking: Boolean = false,
    val currentSpeaker: CharacterVoiceType? = null,
    val isImageGenerationEnabled: Boolean = true
)

private fun defaultInitialActions(): List<SuggestedAction> {
    return listOf(
        SuggestedAction(
            labelAr = "التظاهر بالرعب الشديد والبكاء مع السعال (قناع البراءة)",
            actionType = ActionType.CHILD_FACADE,
            subtitleAr = "إقناعه بأنك مريض ومعدٍ ولن يربح شيئاً من ضربك"
        ),
        SuggestedAction(
            labelAr = "الوقوف ببرود وعرض صفقة استخباراتية (حسابات المفاوضة)",
            actionType = ActionType.STRATEGIC,
            subtitleAr = "عرض معلومات تضاعف دخله من سوق الصباح بدلاً من الشحاذة"
        ),
        SuggestedAction(
            labelAr = "استغلال الطين المتجمد والحجر الحاد لإسقاطه والهرب (دهاء الشارع والفيزياء)",
            actionType = ActionType.DARING,
            subtitleAr = "الفرار السريع في الأزقة الضيقة نحو سوق المدينة"
        )
    )
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    val voiceManager = CharacterVoiceManager(application.applicationContext)

    private val _uiState = MutableStateFlow(
        GameUiState(
            isAutoTtsEnabled = repository.isAutoTtsEnabled(),
            isImageGenerationEnabled = repository.isImageGenerationEnabled()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.initializeGameIfEmpty()
            } catch (e: Throwable) {
                Log.e("GameViewModel", "Failed to initialize game state", e)
            }
            observeDatabase()
            observeVoiceState()
        }
    }

    private fun observeVoiceState() {
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            voiceManager.currentSpeaker.collect { speaker ->
                _uiState.update { it.copy(currentSpeaker = speaker) }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getSessionFlow()
                .catch { e -> Log.e("GameViewModel", "Session flow error", e) }
                .collect { session ->
                    val hasKey = repository.getActiveApiKey().isNotBlank()
                    val masked = if (hasKey) {
                        val key = repository.getActiveApiKey()
                        if (key.length > 8) "${key.take(4)}...${key.takeLast(4)}" else "••••••••"
                    } else ""

                    _uiState.update { current ->
                        current.copy(
                            session = session,
                            isAiActive = hasKey,
                            activeApiKeyMasked = masked,
                            selectedModel = repository.getSelectedModel(),
                            showGameOverDialog = session?.isGameOver == true
                        )
                    }
                }
        }

        viewModelScope.launch {
            repository.getInventoryFlow()
                .catch { e -> Log.e("GameViewModel", "Inventory flow error", e) }
                .collect { inv ->
                    _uiState.update { it.copy(inventory = inv) }
                }
        }

        viewModelScope.launch {
            repository.getTurnLogsFlow()
                .catch { e -> Log.e("GameViewModel", "TurnLogs flow error", e) }
                .collect { logs ->
                    _uiState.update { it.copy(turnLogs = logs) }
                }
        }

        viewModelScope.launch {
            repository.getTheoremsFlow()
                .catch { e -> Log.e("GameViewModel", "Theorems flow error", e) }
                .collect { ths ->
                    _uiState.update { it.copy(theorems = ths) }
                }
        }
    }

    fun submitAction(actionText: String, actionType: ActionType? = null) {
        val currentSession = _uiState.value.session ?: return
        if (currentSession.isGameOver) return
        if (actionText.isBlank()) return

        viewModelScope.launch {
            voiceManager.stop()
            _uiState.update { it.copy(isLoading = true, bannerMessage = null) }
            try {
                val response = repository.executeTurn(
                    playerAction = actionText,
                    actionType = actionType,
                    currentSession = currentSession,
                    currentInventory = _uiState.value.inventory,
                    recentTurnLogs = _uiState.value.turnLogs
                )

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        currentSuggestedActions = if (response.suggestedActions.isNotEmpty()) {
                            response.suggestedActions
                        } else {
                            defaultInitialActions()
                        },
                        bannerMessage = response.newTheorem?.let { "تم اكتشاف قانون فيزيائي جديد: ${it.titleAr}!" }
                    )
                }

                // Auto-read aloud if enabled
                if (_uiState.value.isAutoTtsEnabled) {
                    voiceManager.speakStorySequence(
                        narrative = response.narrativeAr,
                        dialogue = response.npcDialogue
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bannerMessage = "تعذر إكمال الدور: ${e.localizedMessage ?: "خطأ غير معروف"}"
                    )
                }
            }
        }
    }

    fun speakTurn(turnLog: TurnLog) {
        if (_uiState.value.isSpeaking) {
            voiceManager.stop()
        } else {
            voiceManager.speakStorySequence(
                narrative = turnLog.narrativeAr,
                dialogue = turnLog.npcDialogue
            )
        }
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    fun toggleAutoTts() {
        val newSetting = !_uiState.value.isAutoTtsEnabled
        repository.setAutoTtsEnabled(newSetting)
        if (!newSetting) {
            voiceManager.stop()
        }
        _uiState.update { it.copy(isAutoTtsEnabled = newSetting) }
    }

    fun toggleImageGeneration() {
        val newSetting = !_uiState.value.isImageGenerationEnabled
        repository.setImageGenerationEnabled(newSetting)
        _uiState.update { it.copy(isImageGenerationEnabled = newSetting) }
    }

    fun toggleHudExpanded() {
        _uiState.update { it.copy(isHudExpanded = !it.isHudExpanded) }
    }

    fun saveApiKey(newKey: String, selectedModel: String) {
        repository.setCustomApiKey(newKey)
        repository.setSelectedModel(selectedModel)
        val hasKey = repository.getActiveApiKey().isNotBlank()
        val masked = if (hasKey) {
            val key = repository.getActiveApiKey()
            if (key.length > 8) "${key.take(4)}...${key.takeLast(4)}" else "••••••••"
        } else ""

        _uiState.update {
            it.copy(
                isAiActive = hasKey,
                activeApiKeyMasked = masked,
                selectedModel = selectedModel,
                showApiKeyDialog = false,
                bannerMessage = if (hasKey) "تم تفعيل ذكاء Gemini ($selectedModel) بنجاح!" else "تم التبديل إلى وضع الراوي المحلي."
            )
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            voiceManager.stop()
            _uiState.update { it.copy(isLoading = true) }
            repository.resetGame()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentSuggestedActions = defaultInitialActions(),
                    showGameOverDialog = false,
                    bannerMessage = "بدأت الرحلة من جديد في زقاق الصقيع..."
                )
            }
        }
    }

    fun toggleInventory(show: Boolean) {
        _uiState.update { it.copy(showInventorySheet = show) }
    }

    fun toggleTheorems(show: Boolean) {
        _uiState.update { it.copy(showTheoremsSheet = show) }
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
