package com.example.data.gemini

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val MODEL_FLASH_LATEST = "gemini-flash-latest"
        const val MODEL_35_FLASH = "gemini-3.5-flash"
    }

    suspend fun generateTurn(
        apiKey: String,
        modelName: String = MODEL_FLASH_LATEST,
        playerAction: String,
        actionType: ActionType?,
        currentVitals: VitalStats,
        currentSocial: SocialMeters,
        copperCoins: Int,
        manaComprehension: Int,
        inventory: List<InventoryItem>,
        recentTurnLogs: List<TurnLog>
    ): Result<GameEngineTurnResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalArgumentException("No valid Gemini API key provided"))
        }

        val systemPrompt = """
أنت راوي اللعبة والمحرك الحسابي للعبة تقمص الأدوار: "إعادة ولادة الشارع: ذكاء الماضي في عالم السحر" (Reborn in the Alleys).
البطل: ليو (10 سنوات، جسد طفل هزيل يتجمد برداً، لكن عقله مستشار استراتيجي ومهندس فيزياء تطبيقية عمره 45 عاماً).
العالم: إمبراطورية إلدوريا، شتاء قارس، سحر طبقي يحتكره النبلاء كطقوس دينية، بينما ليو يفهم أنه محكوم بقوانين الديناميكا الحرارية والفيزياء.

قواعد المحاكاة:
1. الجوع (0 شبع تام إلى 100 مجاعة قصوى).
2. الطاقة البدنية (0 إلى 100): تنفد بالجهد، إذا وصلت إلى 0 يموت أو يغمى عليه.
3. الدفء (0 إلى 100): البرد القارس ينقص الدفء.
4. مقياس الشك والاشتباه (0 إلى 100):
   - يرتفع عندما يتحدث ليو كشخص بالغ أو يحل مسائل معقدة أو يظهر بروداً غير طبيعي.
   - عند 40%: همسات بأنه ملعون أو طفل غريب.
   - عند 75%: يتدخل محققو معبد إلدوريا للاشتباه باستحواذ شيطاني (Stregoni).
   - عند 100%: إعدام فوري بتهمة الردة/الاستحواذ.
5. قناع البراءة (0 إلى 100): استغلال البكاء والتظاهر بالمرض والطفولة لخفض الشك.

يجب أن يكون الرد بصيغة JSON نظيفة حصراً بالحقول التالية:
{
  "narrative_ar": "نص أدبي حركي وسينمائي بليغ يصف ردة الفعل والبيئة والحواس",
  "npc_dialogue": {
    "speaker_name": "اسم الشخصية الحالية (مثال: روجر الأعور أو حارس البوابة أو الساحر مورفاث)",
    "speech_ar": "نص كلام الشخصية بدقة ونبرة واضحة",
    "tone": "threatening" أو "curious" أو "mocking" أو "shocked" أو "indifferent"
  },
  "stat_changes": {
    "energy_delta": عدد صحيح بين -25 و +25,
    "hunger_delta": عدد صحيح بين -25 و +25 (قيمة موجبة تعني زيادة الجوع، سالبة تعني شبع),
    "warmth_delta": عدد صحيح بين -25 و +25,
    "suspicion_delta": عدد صحيح بين -20 و +35,
    "child_mask_delta": عدد صحيح بين -30 و +30,
    "copper_delta": عدد صحيح لتغير العملات النحاسية,
    "mana_insight_delta": عدد صحيح (زيادة فهم المانا بالفيزياء),
    "reputation_delta": عدد صحيح
  },
  "inventory_updates": {
    "added": [
      { "item_id": "item_code", "name_ar": "اسم الغرض", "desc_ar": "وصفه", "utility_ar": "فائدته" }
    ],
    "removed": ["item_id_if_any"]
  },
  "suggested_actions": [
    { "label_ar": "الخيار 1", "action_type": "strategic", "subtitle_ar": "شرح تكتيكي موجز" },
    { "label_ar": "الخيار 2", "action_type": "child_facade", "subtitle_ar": "شرح تكتيكي موجز" },
    { "label_ar": "الخيار 3", "action_type": "scientific", "subtitle_ar": "شرح تكتيكي موجز" }
  ],
  "discovered_theorem": {
    "id": "theorem_id",
    "title_ar": "اسم القانون المكتشف (مثال: حفظ الطاقة وحركة المانا)",
    "physics_law_ar": "القانون الفيزيائي الأرضي المقابل",
    "mana_application_ar": "كيف يطبقه ليو في عالم السحر لصالحه"
  },
  "game_over": false,
  "game_over_reason_ar": null
}
        """.trimIndent()

        // Build recent context
        val recentLogsText = StringBuilder()
        recentTurnLogs.take(3).reversed().forEach { log ->
            recentLogsText.append("دور سابق: قام ليو بـ: ${log.playerActionText}\n")
            log.npcDialogue?.let { recentLogsText.append("رد ${it.speakerName}: ${it.speechAr}\n") }
        }

        val inventoryNames = inventory.joinToString(", ") { "${it.nameAr} (x${it.quantity})" }

        val userPrompt = """
الحالة الحالية للبطل ليو:
- الطاقة البدنية: ${currentVitals.energy}/100
- الجوع: ${currentVitals.hunger}/100
- الدفء: ${currentVitals.warmth}/100
- مقياس الشك: ${currentSocial.suspicion}/100
- قناع البراءة: ${currentSocial.childMask}/100
- العملات النحاسية: $copperCoins
- فهم المانا: $manaComprehension
- محتويات الحقيبة: $inventoryNames

السياق الحديث:
$recentLogsText

قرار ليو الحالي:
نوع القرار: ${actionType?.titleAr ?: "قرار حر"}
نص القرار: $playerAction

قم بتحليل النتائج والعواقب بحيادية واقعية قاسية ومكافأة الذكاء الهندسي، وأرجع استجابة JSON كاملة وفق المخطط المطلوب.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.75)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$modelName:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $errorBody")
                return@withContext Result.failure(Exception("HTTP ${response.code}: $errorBody"))
            }

            val responseBody = response.body?.string().orEmpty()
            val parsedResult = parseGeminiResponse(responseBody)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Gemini API", e)
            Result.failure(e)
        }
    }

    private fun parseGeminiResponse(rawJson: String): GameEngineTurnResponse {
        val root = JSONObject(rawJson)
        val candidates = root.optJSONArray("candidates") ?: throw IllegalArgumentException("No candidates returned")
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        var rawText = parts.getJSONObject(0).getString("text").trim()

        // Clean markdown fencing if model included it
        if (rawText.startsWith("```json")) {
            rawText = rawText.removePrefix("```json")
        } else if (rawText.startsWith("```")) {
            rawText = rawText.removePrefix("```")
        }
        if (rawText.endsWith("```")) {
            rawText = rawText.removeSuffix("```")
        }
        rawText = rawText.trim()

        val json = JSONObject(rawText)
        val narrativeAr = json.optString("narrative_ar", "تستمر الأحداث في أزقة إلدوريا...")

        var npcDialogue: NpcDialogue? = null
        val npcObj = json.optJSONObject("npc_dialogue")
        if (npcObj != null && npcObj.has("speech_ar") && npcObj.getString("speech_ar").isNotBlank()) {
            val speaker = npcObj.optString("speaker_name", "شخص غريب")
            val speech = npcObj.optString("speech_ar", "")
            val toneStr = npcObj.optString("tone", "threatening")
            val tone = when (toneStr.lowercase()) {
                "curious" -> NpcTone.CURIOUS
                "mocking" -> NpcTone.MOCKING
                "shocked" -> NpcTone.SHOCKED
                "indifferent" -> NpcTone.INDIFFERENT
                else -> NpcTone.THREATENING
            }
            npcDialogue = NpcDialogue(speaker, speech, tone)
        }

        val statObj = json.optJSONObject("stat_changes") ?: JSONObject()
        val energyDelta = statObj.optInt("energy_delta", 0)
        val hungerDelta = statObj.optInt("hunger_delta", 0)
        val warmthDelta = statObj.optInt("warmth_delta", 0)
        val suspicionDelta = statObj.optInt("suspicion_delta", 0)
        val childMaskDelta = statObj.optInt("child_mask_delta", 0)
        val copperDelta = statObj.optInt("copper_delta", 0)
        val manaInsightDelta = statObj.optInt("mana_insight_delta", 0)
        val reputationDelta = statObj.optInt("reputation_delta", 0)

        // Inventory
        val itemsAdded = mutableListOf<InventoryItem>()
        val itemsRemoved = mutableListOf<String>()
        val invObj = json.optJSONObject("inventory_updates")
        if (invObj != null) {
            val addedArr = invObj.optJSONArray("added")
            if (addedArr != null) {
                for (i in 0 until addedArr.length()) {
                    val it = addedArr.getJSONObject(i)
                    itemsAdded.add(
                        InventoryItem(
                            itemId = it.optString("item_id", "item_${System.currentTimeMillis()}_$i"),
                            nameAr = it.optString("name_ar", "غرض جديد"),
                            descAr = it.optString("desc_ar", ""),
                            utilityAr = it.optString("utility_ar", "")
                        )
                    )
                }
            }
            val remArr = invObj.optJSONArray("removed")
            if (remArr != null) {
                for (i in 0 until remArr.length()) {
                    itemsRemoved.add(remArr.getString(i))
                }
            }
        }

        // Suggested Actions
        val actions = mutableListOf<SuggestedAction>()
        val actionsArr = json.optJSONArray("suggested_actions")
        if (actionsArr != null) {
            for (i in 0 until actionsArr.length()) {
                val act = actionsArr.getJSONObject(i)
                val typeStr = act.optString("action_type", "strategic").lowercase()
                val actType = when (typeStr) {
                    "child_facade" -> ActionType.CHILD_FACADE
                    "daring" -> ActionType.DARING
                    "scientific" -> ActionType.SCIENTIFIC
                    else -> ActionType.STRATEGIC
                }
                actions.add(
                    SuggestedAction(
                        labelAr = act.optString("label_ar", "اتخاذ خطوة محسوبة"),
                        actionType = actType,
                        subtitleAr = act.optString("subtitle_ar", "")
                    )
                )
            }
        }

        // Theorem discovered
        var theorem: DiscoveredTheorem? = null
        val thObj = json.optJSONObject("discovered_theorem")
        if (thObj != null && thObj.has("title_ar") && thObj.getString("title_ar").isNotBlank()) {
            theorem = DiscoveredTheorem(
                id = thObj.optString("id", "th_${System.currentTimeMillis()}"),
                titleAr = thObj.getString("title_ar"),
                physicsLawAr = thObj.optString("physics_law_ar", "مبدأ فيزيائي"),
                manaApplicationAr = thObj.optString("mana_application_ar", "تطبيق سحري"),
                discoveryTurn = 1
            )
        }

        val isGameOver = json.optBoolean("game_over", false)
        val gameOverReason = if (json.has("game_over_reason_ar") && !json.isNull("game_over_reason_ar")) {
            json.getString("game_over_reason_ar")
        } else null

        return GameEngineTurnResponse(
            narrativeAr = narrativeAr,
            npcDialogue = npcDialogue,
            energyDelta = energyDelta,
            hungerDelta = hungerDelta,
            warmthDelta = warmthDelta,
            suspicionDelta = suspicionDelta,
            childMaskDelta = childMaskDelta,
            copperDelta = copperDelta,
            manaInsightDelta = manaInsightDelta,
            reputationDelta = reputationDelta,
            itemsAdded = itemsAdded,
            itemsRemoved = itemsRemoved,
            suggestedActions = actions,
            isGameOver = isGameOver,
            gameOverReasonAr = gameOverReason,
            newTheorem = theorem
        )
    }
}
