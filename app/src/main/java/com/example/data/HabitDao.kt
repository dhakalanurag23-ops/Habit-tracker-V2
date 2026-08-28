package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.HabitTask
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits_and_tasks ORDER BY isCompletedToday ASC, scheduledHour ASC, scheduledMinute ASC")
    fun getAllHabitsAndTasks(): Flow<List<HabitTask>>

    @Query("SELECT * FROM habits_and_tasks WHERE isHabit = 1 ORDER BY isCompletedToday ASC, scheduledHour ASC")
    fun getHabits(): Flow<List<HabitTask>>

    @Query("SELECT * FROM habits_and_tasks WHERE isHabit = 0 ORDER BY isCompletedToday ASC, scheduledHour ASC")
    fun getTasks(): Flow<List<HabitTask>>

    @Query("SELECT * FROM habits_and_tasks WHERE id = :id")
    suspend fun getById(id: Long): HabitTask?

    @Query("SELECT * FROM habits_and_tasks")
    suspend fun getAllSnapshot(): List<HabitTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HabitTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HabitTask>): List<Long>

    @Update
    suspend fun update(item: HabitTask)

    @Delete
    suspend fun delete(item: HabitTask)

    @Query("DELETE FROM habits_and_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits_and_tasks")
    suspend fun deleteAll()

    @Query("UPDATE habits_and_tasks SET isCompletedToday = 0 WHERE lastCompletedDate != :todayDate")
    suspend fun resetDayCompletionsIfNotToday(todayDate: String)
}
