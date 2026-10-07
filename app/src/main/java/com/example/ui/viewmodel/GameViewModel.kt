package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val bannerMessage: String? = null
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

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeGameIfEmpty()
            observeDatabase()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getSessionFlow().collect { session ->
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
            repository.getInventoryFlow().collect { inv ->
                _uiState.update { it.copy(inventory = inv) }
            }
        }

        viewModelScope.launch {
            repository.getTurnLogsFlow().collect { logs ->
                _uiState.update { it.copy(turnLogs = logs) }
            }
        }

        viewModelScope.launch {
            repository.getTheoremsFlow().collect { ths ->
                _uiState.update { it.copy(theorems = ths) }
            }
        }
    }

    fun submitAction(actionText: String, actionType: ActionType? = null) {
        val currentSession = _uiState.value.session ?: return
        if (currentSession.isGameOver) return
        if (actionText.isBlank()) return

        viewModelScope.launch {
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
}
