package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_session")
data class GameSessionEntity(
    @PrimaryKey
    val id: Int = 1,
    val turnCount: Int = 1,
    val energy: Int = 25,
    val hunger: Int = 75,
    val warmth: Int = 20,
    val suspicion: Int = 0,
    val childMask: Int = 100,
    val manaComprehension: Int = 10,
    val streetReputation: Int = 5,
    val copperCoins: Int = 0,
    val isGameOver: Boolean = false,
    val gameOverReason: String? = null,
    val currentNpcSpeaker: String? = null,
    val currentNpcSpeech: String? = null,
    val currentNpcTone: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey
    val id: String,
    val nameAr: String,
    val descAr: String,
    val utilityAr: String,
    val quantity: Int
)

@Entity(tableName = "turn_logs")
data class TurnLogEntity(
    @PrimaryKey
    val id: String,
    val turnNumber: Int,
    val playerActionText: String,
    val actionType: String?,
    val narrativeAr: String,
    val npcSpeaker: String?,
    val npcSpeech: String?,
    val npcTone: String?,
    val sceneImagePath: String? = null,
    val timestamp: Long
)

@Entity(tableName = "theorems")
data class TheoremEntity(
    @PrimaryKey
    val id: String,
    val titleAr: String,
    val physicsLawAr: String,
    val manaApplicationAr: String,
    val discoveryTurn: Int
)
