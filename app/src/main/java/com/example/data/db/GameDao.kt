package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_session WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<GameSessionEntity?>

    @Query("SELECT * FROM game_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): GameSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: GameSessionEntity)

    @Query("SELECT * FROM inventory_items")
    fun getInventoryFlow(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    @Query("DELETE FROM inventory_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()

    @Query("SELECT * FROM turn_logs ORDER BY turnNumber DESC")
    fun getTurnLogsFlow(): Flow<List<TurnLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurnLog(turnLog: TurnLogEntity)

    @Query("DELETE FROM turn_logs")
    suspend fun clearTurnLogs()

    @Query("SELECT * FROM theorems ORDER BY discoveryTurn ASC")
    fun getTheoremsFlow(): Flow<List<TheoremEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheorem(theorem: TheoremEntity)

    @Query("DELETE FROM theorems")
    suspend fun clearTheorems()
}
