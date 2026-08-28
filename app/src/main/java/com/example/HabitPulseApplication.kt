package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitPulseApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { HabitRepository(database.habitDao(), database.foodDao(), database.waterDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.syncDailyState()
        }
    }
}
