package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.data.db.AppDatabase
import com.example.data.db.GameSessionEntity
import com.example.data.db.InventoryEntity
import com.example.data.db.TheoremEntity
import com.example.data.db.TurnLogEntity
import com.example.data.engine.DeterministicGameEngine
import com.example.data.gemini.GeminiApiClient
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GameRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val geminiClient: GeminiApiClient = GeminiApiClient()
) {
    private val dao = database.gameDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("reborn_alleys_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val PREF_SELECTED_MODEL = "selected_gemini_model"
        private const val PREF_GENERATE_IMAGES = "generate_scene_images"
        private const val PREF_AUTO_TTS = "auto_tts_reading"
    }

    fun isImageGenerationEnabled(): Boolean {
        return prefs.getBoolean(PREF_GENERATE_IMAGES, true)
    }

    fun setImageGenerationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_GENERATE_IMAGES, enabled).apply()
    }

    fun isAutoTtsEnabled(): Boolean {
        return prefs.getBoolean(PREF_AUTO_TTS, true)
    }

    fun setAutoTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_AUTO_TTS, enabled).apply()
    }

    fun getActiveApiKey(): String {
        val custom = prefs.getString(PREF_CUSTOM_API_KEY, "")?.trim().orEmpty()
        if (custom.isNotBlank()) return custom
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(PREF_SELECTED_MODEL, GeminiApiClient.MODEL_FLASH_LATEST)
            ?: GeminiApiClient.MODEL_FLASH_LATEST
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(PREF_SELECTED_MODEL, model).apply()
    }

    fun getSessionFlow(): Flow<GameSessionEntity?> = dao.getSessionFlow()

    fun getInventoryFlow(): Flow<List<InventoryItem>> = dao.getInventoryFlow().map { entities ->
        entities.map {
            InventoryItem(
                itemId = it.id,
                nameAr = it.nameAr,
                descAr = it.descAr,
                utilityAr = it.utilityAr,
                quantity = it.quantity
            )
        }
    }

    fun getTurnLogsFlow(): Flow<List<TurnLog>> = dao.getTurnLogsFlow().map { entities ->
        entities.map {
            TurnLog(
                id = it.id,
                turnNumber = it.turnNumber,
                playerActionText = it.playerActionText,
                actionType = it.actionType?.let { t -> runCatching { ActionType.valueOf(t) }.getOrNull() },
                narrativeAr = it.narrativeAr,
                npcDialogue = if (it.npcSpeaker != null && it.npcSpeech != null) {
                    val tone = it.npcTone?.let { tn -> runCatching { NpcTone.valueOf(tn) }.getOrNull() } ?: NpcTone.THREATENING
                    NpcDialogue(it.npcSpeaker, it.npcSpeech, tone)
                } else null,
                sceneImagePath = it.sceneImagePath,
                timestamp = it.timestamp
            )
        }
    }

    fun getTheoremsFlow(): Flow<List<DiscoveredTheorem>> = dao.getTheoremsFlow().map { entities ->
        entities.map {
            DiscoveredTheorem(
                id = it.id,
                titleAr = it.titleAr,
                physicsLawAr = it.physicsLawAr,
                manaApplicationAr = it.manaApplicationAr,
                discoveryTurn = it.discoveryTurn
            )
        }
    }

    suspend fun initializeGameIfEmpty() {
        try {
            val session = dao.getSession()
            if (session == null) {
                resetGame()
            }
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error accessing database in initializeGameIfEmpty", e)
            try {
                resetGame()
            } catch (t: Throwable) {
                Log.e("GameRepository", "Error resetting game", t)
            }
        }
    }

    suspend fun resetGame() {
        try {
            dao.clearInventory()
            dao.clearTurnLogs()
            dao.clearTheorems()

            val initialSession = GameSessionEntity(
                id = 1,
                turnCount = 1,
                energy = 25,
                hunger = 75,
                warmth = 20,
                suspicion = 0,
                childMask = 100,
                manaComprehension = 10,
                streetReputation = 5,
                copperCoins = 0,
                isGameOver = false,
                gameOverReason = null,
                currentNpcSpeaker = "روجر الأعور (مشرف عصابة المتسولين)",
                currentNpcSpeech = "استيقظ يا ليو الكسول! أين العملات النحاسية الخمس التي كان يُفترض أن تجمعها من سوق الصباح؟ إذا لم تسلمني المال الآن، فسأرميك خارج الأسوار للذئاب تتغذى على عظامك الهزيلة!",
                currentNpcTone = NpcTone.THREATENING.name
            )
            dao.saveSession(initialSession)

            // Seed initial items
            dao.insertItem(
                InventoryEntity(
                    id = "stolen_chalk",
                    nameAr = "قطعة طبشور مسروقة",
                    descAr = "قطعة طبشور بيضاء صلبة خبأتها في بطانة معطفك المهترئ.",
                    utilityAr = "تصلح لرسم مسارات المانا، وكتابة معادلات فيزيائية على الجدران، أو ترك إشارات سرية.",
                    quantity = 1
                )
            )
            dao.insertItem(
                InventoryEntity(
                    id = "sharp_flint",
                    nameAr = "حجر صوان حاد",
                    descAr = "حجر ناري رمادي ذو حافة مسننة وقاسية.",
                    utilityAr = "توليد شرر فوري بالاحتكاك، أو الدفاع المباغت في الزوايا المظلمة.",
                    quantity = 1
                )
            )

            // Seed initial prologue log
            val prologueLog = TurnLogEntity(
                id = UUID.randomUUID().toString(),
                turnNumber = 1,
                playerActionText = "البداية: الاستيقاظ في زقاق الفحم المتجمد",
                actionType = null,
                narrativeAr = "تفتح عينيك الثقيلتين على لسعات الصقيع التي تنخر جسدك النحيل. أنت متكئ على برميل نبيذ خشبي مكسور، ورائحة الفحم والرطوبة تملأ رئتيك الصغيرتين. ذاكرتك كمدير استراتيجي ومهندس فيزياء تطبيقية ذي 45 عاماً واضحة كضوء النهار، لكن عندما ترفع يديك تجدهما يدي صبي متسخ في العاشرة من عمره يدعى «ليو». يقطع صمت الفجر ركلة عنيفة تهز البرميل بقدم ثقيلة لبلطجي الأزقة «روجر الأعور».",
                npcSpeaker = "روجر الأعور",
                npcSpeech = "استيقظ يا ليو الكسول! أين العملات النحاسية الخمس التي كان يُفترض أن تجمعها من سوق الصباح؟ إذا لم تسلمني المال الآن، فسأرميك خارج الأسوار للذئاب تتغذى على عظامك الهزيلة!",
                npcTone = NpcTone.THREATENING.name,
                timestamp = System.currentTimeMillis()
            )
            dao.insertTurnLog(prologueLog)
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error executing resetGame", e)
        }
    }

    suspend fun executeTurn(
        playerAction: String,
        actionType: ActionType?,
        currentSession: GameSessionEntity,
        currentInventory: List<InventoryItem>,
        recentTurnLogs: List<TurnLog>
    ): GameEngineTurnResponse {
        val apiKey = getActiveApiKey()
        val currentVitals = VitalStats(
            energy = currentSession.energy,
            hunger = currentSession.hunger,
            warmth = currentSession.warmth
        )
        val currentSocial = SocialMeters(
            suspicion = currentSession.suspicion,
            childMask = currentSession.childMask
        )

        val turnResponse: GameEngineTurnResponse = if (apiKey.isNotBlank()) {
            val result = geminiClient.generateTurn(
                apiKey = apiKey,
                modelName = getSelectedModel(),
                playerAction = playerAction,
                actionType = actionType,
                currentVitals = currentVitals,
                currentSocial = currentSocial,
                copperCoins = currentSession.copperCoins,
                manaComprehension = currentSession.manaComprehension,
                inventory = currentInventory,
                recentTurnLogs = recentTurnLogs
            )
            result.getOrElse { error ->
                Log.w("GameRepository", "Gemini call failed, falling back to deterministic engine: ${error.message}")
                DeterministicGameEngine.processTurn(
                    playerAction = playerAction,
                    actionType = actionType,
                    currentVitals = currentVitals,
                    currentSocial = currentSocial,
                    copperCoins = currentSession.copperCoins,
                    manaComprehension = currentSession.manaComprehension,
                    inventory = currentInventory,
                    turnCount = currentSession.turnCount
                )
            }
        } else {
            DeterministicGameEngine.processTurn(
                playerAction = playerAction,
                actionType = actionType,
                currentVitals = currentVitals,
                currentSocial = currentSocial,
                copperCoins = currentSession.copperCoins,
                manaComprehension = currentSession.manaComprehension,
                inventory = currentInventory,
                turnCount = currentSession.turnCount
            )
        }

        // Apply stat changes with clamping
        val newEnergy = (currentSession.energy + turnResponse.energyDelta).coerceIn(0, 100)
        val newHunger = (currentSession.hunger + turnResponse.hungerDelta).coerceIn(0, 100)
        val newWarmth = (currentSession.warmth + turnResponse.warmthDelta).coerceIn(0, 100)
        val newSuspicion = (currentSession.suspicion + turnResponse.suspicionDelta).coerceIn(0, 100)
        val newChildMask = (currentSession.childMask + turnResponse.childMaskDelta).coerceIn(0, 100)
        val newCopper = (currentSession.copperCoins + turnResponse.copperDelta).coerceAtLeast(0)
        val newMana = currentSession.manaComprehension + turnResponse.manaInsightDelta
        val newRep = currentSession.streetReputation + turnResponse.reputationDelta

        // Check death or failure conditions
        var gameOver = turnResponse.isGameOver
        var gameOverReason = turnResponse.gameOverReasonAr

        if (newEnergy <= 0) {
            gameOver = true
            gameOverReason = "انهيار الجسد: لم يحتمل جسد ليو النحيل الإرهاق الشديد وتوقف قلبه الصغير عن النبض."
        } else if (newWarmth <= 0) {
            gameOver = true
            gameOverReason = "الموت صقيعاً: لسعات الشتاء القارس في إلدوريا جمدت أطراف ليو ومات من انخفاض الحرارة الحاد."
        } else if (newHunger >= 100) {
            gameOver = true
            gameOverReason = "الموت جوعاً: تآكلت قوى الصبي ليو تماماً بعد أيام من الحرمان دون طعام."
        } else if (newSuspicion >= 100) {
            gameOver = true
            gameOverReason = "حرق الردة والاستحواذ: ألقت دورية محققي معبد إلدوريا القبض عليك بتهمة تلبس روح شيطانية (Stregoni) وتم إعدامك في الساحة العامة!"
        }

        val nextTurnNumber = currentSession.turnCount + 1
        val updatedSession = currentSession.copy(
            turnCount = nextTurnNumber,
            energy = newEnergy,
            hunger = newHunger,
            warmth = newWarmth,
            suspicion = newSuspicion,
            childMask = newChildMask,
            copperCoins = newCopper,
            manaComprehension = newMana,
            streetReputation = newRep,
            isGameOver = gameOver,
            gameOverReason = gameOverReason,
            currentNpcSpeaker = turnResponse.npcDialogue?.speakerName,
            currentNpcSpeech = turnResponse.npcDialogue?.speechAr,
            currentNpcTone = turnResponse.npcDialogue?.tone?.name,
            lastUpdated = System.currentTimeMillis()
        )
        dao.saveSession(updatedSession)

        // Update inventory
        turnResponse.itemsAdded.forEach { item ->
            dao.insertItem(
                InventoryEntity(
                    id = item.itemId,
                    nameAr = item.nameAr,
                    descAr = item.descAr,
                    utilityAr = item.utilityAr,
                    quantity = item.quantity
                )
            )
        }
        turnResponse.itemsRemoved.forEach { id ->
            dao.deleteItem(id)
        }

        // Save new theorem if discovered
        turnResponse.newTheorem?.let { th ->
            dao.insertTheorem(
                TheoremEntity(
                    id = th.id,
                    titleAr = th.titleAr,
                    physicsLawAr = th.physicsLawAr,
                    manaApplicationAr = th.manaApplicationAr,
                    discoveryTurn = nextTurnNumber
                )
            )
        }

        // Generate scene illustration if enabled
        var sceneImagePath: String? = null
        if (isImageGenerationEnabled() && apiKey.isNotBlank()) {
            try {
                val sceneFile = java.io.File(
                    context.filesDir,
                    "scenes/scene_turn_${nextTurnNumber}_${System.currentTimeMillis()}.jpg"
                )
                val imgResult = geminiClient.generateSceneIllustration(
                    apiKey = apiKey,
                    sceneSummary = turnResponse.narrativeAr.take(180),
                    characterName = turnResponse.npcDialogue?.speakerName,
                    outputFile = sceneFile
                )
                sceneImagePath = imgResult.getOrNull()
            } catch (e: Exception) {
                Log.w("GameRepository", "Scene image generation skipped: ${e.message}")
            }
        }

        // Insert turn log
        dao.insertTurnLog(
            TurnLogEntity(
                id = UUID.randomUUID().toString(),
                turnNumber = nextTurnNumber,
                playerActionText = playerAction,
                actionType = actionType?.name,
                narrativeAr = turnResponse.narrativeAr,
                npcSpeaker = turnResponse.npcDialogue?.speakerName,
                npcSpeech = turnResponse.npcDialogue?.speechAr,
                npcTone = turnResponse.npcDialogue?.tone?.name,
                sceneImagePath = sceneImagePath,
                timestamp = System.currentTimeMillis()
            )
        )

        return turnResponse.copy(sceneImagePath = sceneImagePath)
    }
}
