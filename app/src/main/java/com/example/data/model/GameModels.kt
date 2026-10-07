package com.example.data.model

enum class ActionType(val titleAr: String, val badgeColorHex: Long) {
    STRATEGIC("حسابات المفاوضة", 0xFF6366F1),
    CHILD_FACADE("قناع البراءة", 0xFF3B82F6),
    DARING("دهاء الشارع والمراوغة", 0xFF10B981),
    SCIENTIFIC("الفيزياء والسحر", 0xFF06B6D4)
}

enum class NpcTone(val labelAr: String, val colorHex: Long) {
    THREATENING("تهديد وغضب", 0xFFEF4444),
    CURIOUS("فضول وريبة", 0xFFF59E0B),
    MOCKING("سخرية واستهزاء", 0xFFA855F7),
    SHOCKED("صدمة وذهول", 0xFF38BDF8),
    INDIFFERENT("حياد وبرود", 0xFF94A3B8)
}

data class VitalStats(
    val energy: Int = 25,
    val hunger: Int = 75,
    val warmth: Int = 20
) {
    fun copyClamped(
        energyDelta: Int = 0,
        hungerDelta: Int = 0,
        warmthDelta: Int = 0
    ): VitalStats {
        return VitalStats(
            energy = (energy + energyDelta).coerceIn(0, 100),
            hunger = (hunger + hungerDelta).coerceIn(0, 100),
            warmth = (warmth + warmthDelta).coerceIn(0, 100)
        )
    }
}

data class SocialMeters(
    val suspicion: Int = 0,
    val childMask: Int = 100
) {
    fun copyClamped(
        suspicionDelta: Int = 0,
        childMaskDelta: Int = 0
    ): SocialMeters {
        return SocialMeters(
            suspicion = (suspicion + suspicionDelta).coerceIn(0, 100),
            childMask = (childMask + childMaskDelta).coerceIn(0, 100)
        )
    }
}

data class InventoryItem(
    val itemId: String,
    val nameAr: String,
    val descAr: String,
    val utilityAr: String,
    val quantity: Int = 1
)

data class SuggestedAction(
    val labelAr: String,
    val actionType: ActionType,
    val subtitleAr: String = ""
)

data class NpcDialogue(
    val speakerName: String,
    val speechAr: String,
    val tone: NpcTone = NpcTone.THREATENING
)

enum class CharacterVoiceType(val labelAr: String, val pitch: Float, val speedRate: Float) {
    NARRATOR("الراوي", 0.95f, 0.95f),
    LEO("الصبي ليو", 1.35f, 1.05f),
    ROGER("روجر الأعور", 0.70f, 0.88f),
    MORVATH("الساحر مورفاث", 1.15f, 1.00f),
    GUARD("حارس الدورية", 0.82f, 0.95f),
    MERCHANT("التاجر", 1.05f, 1.10f);

    companion object {
        fun fromSpeaker(speakerName: String?): CharacterVoiceType {
            if (speakerName == null) return NARRATOR
            return when {
                speakerName.contains("روجر") -> ROGER
                speakerName.contains("مورفاث") || speakerName.contains("ساحر") -> MORVATH
                speakerName.contains("حارس") || speakerName.contains("فارس") -> GUARD
                speakerName.contains("تاجر") || speakerName.contains("خباز") -> MERCHANT
                speakerName.contains("ليو") -> LEO
                else -> NARRATOR
            }
        }
    }
}

data class DiscoveredTheorem(
    val id: String,
    val titleAr: String,
    val physicsLawAr: String,
    val manaApplicationAr: String,
    val discoveryTurn: Int
)

data class TurnLog(
    val id: String,
    val turnNumber: Int,
    val playerActionText: String,
    val actionType: ActionType?,
    val narrativeAr: String,
    val npcDialogue: NpcDialogue? = null,
    val statSummaryAr: String = "",
    val theoremUnlocked: DiscoveredTheorem? = null,
    val sceneImagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameEngineTurnResponse(
    val narrativeAr: String,
    val npcDialogue: NpcDialogue? = null,
    val energyDelta: Int = 0,
    val hungerDelta: Int = 0,
    val warmthDelta: Int = 0,
    val suspicionDelta: Int = 0,
    val childMaskDelta: Int = 0,
    val copperDelta: Int = 0,
    val manaInsightDelta: Int = 0,
    val reputationDelta: Int = 0,
    val itemsAdded: List<InventoryItem> = emptyList(),
    val itemsRemoved: List<String> = emptyList(),
    val suggestedActions: List<SuggestedAction> = emptyList(),
    val isGameOver: Boolean = false,
    val gameOverReasonAr: String? = null,
    val newTheorem: DiscoveredTheorem? = null,
    val sceneImagePath: String? = null
)
