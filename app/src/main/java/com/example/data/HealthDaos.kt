package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.FoodEntry
import com.example.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_logs WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getFoodLogsForDate(dateStr: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_logs")
    suspend fun getAllSnapshot(): List<FoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FoodEntry>): List<Long>

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun deleteAll()
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs")
    suspend fun getAllSnapshot(): List<WaterLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WaterLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WaterLog>): List<Long>

    @Delete
    suspend fun delete(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun deleteAll()
}
