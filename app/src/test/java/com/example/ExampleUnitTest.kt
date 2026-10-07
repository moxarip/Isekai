package com.example

import com.example.data.engine.DeterministicGameEngine
import com.example.data.model.ActionType
import com.example.data.model.SocialMeters
import com.example.data.model.VitalStats
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testInitialRogerConfrontation_ChildFacade() {
        val vitals = VitalStats(energy = 25, hunger = 75, warmth = 20)
        val social = SocialMeters(suspicion = 0, childMask = 100)

        val turnResponse = DeterministicGameEngine.processTurn(
            playerAction = "التظاهر بالرعب الشديد والبكاء مع السعال",
            actionType = ActionType.CHILD_FACADE,
            currentVitals = vitals,
            currentSocial = social,
            copperCoins = 0,
            manaComprehension = 10,
            inventory = emptyList(),
            turnCount = 1
        )

        assertNotNull(turnResponse.narrativeAr)
        assertTrue(turnResponse.narrativeAr.contains("البكاء"))
        assertNotNull(turnResponse.npcDialogue)
        assertEquals("روجر الأعور", turnResponse.npcDialogue?.speakerName)
        assertTrue(turnResponse.suspicionDelta <= 0) // Suspicion decreases or stays safe
        assertEquals(3, turnResponse.suggestedActions.size)
    }

    @Test
    fun testInitialRogerConfrontation_StrategicDealIncreasesSuspicion() {
        val vitals = VitalStats(energy = 25, hunger = 75, warmth = 20)
        val social = SocialMeters(suspicion = 0, childMask = 100)

        val turnResponse = DeterministicGameEngine.processTurn(
            playerAction = "الوقوف ببرود وعرض صفقة استخباراتية",
            actionType = ActionType.STRATEGIC,
            currentVitals = vitals,
            currentSocial = social,
            copperCoins = 0,
            manaComprehension = 10,
            inventory = emptyList(),
            turnCount = 1
        )

        assertTrue(turnResponse.suspicionDelta > 10) // Adult speech raises suspicion!
        assertTrue(turnResponse.copperDelta > 0) // Earns coin
    }

    @Test
    fun testClampingOfVitals() {
        val vitals = VitalStats(energy = 95, hunger = 10, warmth = 10)
        val clamped = vitals.copyClamped(energyDelta = 20, hungerDelta = -50, warmthDelta = -30)

        assertEquals(100, clamped.energy)
        assertEquals(0, clamped.hunger)
        assertEquals(0, clamped.warmth)
    }
}
