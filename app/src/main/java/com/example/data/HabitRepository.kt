package com.example.data

import com.example.model.FoodEntry
import com.example.model.HabitTask
import com.example.model.WaterLog
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val foodDao: FoodDao,
    private val waterDao: WaterDao
) {

    // Habits & Tasks
    val allItems: Flow<List<HabitTask>> = habitDao.getAllHabitsAndTasks()
    val habits: Flow<List<HabitTask>> = habitDao.getHabits()
    val tasks: Flow<List<HabitTask>> = habitDao.getTasks()

    // Food & Water Flows
    fun getFoodLogsForDate(dateStr: String): Flow<List<FoodEntry>> = foodDao.getFoodLogsForDate(dateStr)
    val allFoodLogs: Flow<List<FoodEntry>> = foodDao.getAllFoodLogs()

    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterLog>> = waterDao.getWaterLogsForDate(dateStr)
    val allWaterLogs: Flow<List<WaterLog>> = waterDao.getAllWaterLogs()

    suspend fun getAllSnapshot(): List<HabitTask> = habitDao.getAllSnapshot()
    suspend fun getAllFoodSnapshot(): List<FoodEntry> = foodDao.getAllSnapshot()
    suspend fun getAllWaterSnapshot(): List<WaterLog> = waterDao.getAllSnapshot()

    suspend fun getById(id: Long): HabitTask? = habitDao.getById(id)

    suspend fun insert(item: HabitTask): Long = habitDao.insert(item)
    suspend fun insertAll(items: List<HabitTask>): List<Long> = habitDao.insertAll(items)
    suspend fun update(item: HabitTask) = habitDao.update(item)
    suspend fun delete(item: HabitTask) = habitDao.delete(item)
    suspend fun deleteById(id: Long) = habitDao.deleteById(id)

    // Food operations
    suspend fun insertFood(entry: FoodEntry): Long = foodDao.insert(entry)
    suspend fun insertAllFood(entries: List<FoodEntry>): List<Long> = foodDao.insertAll(entries)
    suspend fun deleteFood(entry: FoodEntry) = foodDao.delete(entry)
    suspend fun deleteFoodById(id: Long) = foodDao.deleteById(id)

    // Water operations
    suspend fun insertWater(log: WaterLog): Long = waterDao.insert(log)
    suspend fun insertAllWater(logs: List<WaterLog>): List<Long> = waterDao.insertAll(logs)
    suspend fun deleteWater(log: WaterLog) = waterDao.delete(log)
    suspend fun deleteWaterById(id: Long) = waterDao.deleteById(id)

    suspend fun toggleCompletion(id: Long): HabitTask? {
        val item = habitDao.getById(id) ?: return null
        val today = HabitTask.getCurrentDateString()
        val isNowCompleted = !item.isCompletedToday

        val newStreak = if (isNowCompleted) {
            item.streakCount + 1
        } else {
            maxOf(0, item.streakCount - 1)
        }
        val bestStreak = maxOf(item.bestStreak, newStreak)

        // Parse history
        val historyList = parseHistoryJson(item.completedHistoryJson).toMutableList()
        if (isNowCompleted) {
            if (!historyList.contains(today)) {
                historyList.add(today)
            }
        } else {
            historyList.remove(today)
        }

        val updated = item.copy(
            isCompletedToday = isNowCompleted,
            streakCount = if (item.isHabit) newStreak else item.streakCount,
            bestStreak = if (item.isHabit) bestStreak else item.bestStreak,
            lastCompletedDate = if (isNowCompleted) today else item.lastCompletedDate,
            completedHistoryJson = historyList.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
        )
        habitDao.update(updated)
        return updated
    }

    suspend fun syncDailyState() {
        val today = HabitTask.getCurrentDateString()
        habitDao.resetDayCompletionsIfNotToday(today)
    }

    suspend fun deleteAll() {
        habitDao.deleteAll()
        foodDao.deleteAll()
        waterDao.deleteAll()
    }

    suspend fun replaceAll(
        items: List<HabitTask>,
        foods: List<FoodEntry> = emptyList(),
        waters: List<WaterLog> = emptyList()
    ) {
        habitDao.deleteAll()
        foodDao.deleteAll()
        waterDao.deleteAll()
        if (items.isNotEmpty()) {
            habitDao.insertAll(items)
        }
        if (foods.isNotEmpty()) {
            foodDao.insertAll(foods)
        }
        if (waters.isNotEmpty()) {
            waterDao.insertAll(waters)
        }
    }

    private fun parseHistoryJson(json: String): List<String> {
        if (json.isBlank() || json == "[]") return emptyList()
        return json.replace("[", "").replace("]", "").replace("\"", "").split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
