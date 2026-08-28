package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyHealthSummary
import com.example.model.FoodEntry
import com.example.model.MealType
import com.example.model.WaterLog
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.LocalThemeConfig
import com.example.ui.theme.aestheticContainer

@Composable
fun HealthNutritionCard(
    summary: DailyHealthSummary,
    foodLogs: List<FoodEntry>,
    waterLogs: List<WaterLog>,
    onLogWater: (Int) -> Unit,
    onDeleteWater: (WaterLog) -> Unit,
    onAddFood: (FoodEntry) -> Unit,
    onDeleteFood: (FoodEntry) -> Unit,
    onOpenMealSnap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent
    val density = themeConfig.density

    var showAddFoodDialog by remember { mutableStateOf(false) }

    val waterProgress by animateFloatAsState(
        targetValue = if (summary.waterGoalMl > 0) (summary.waterConsumedTodayMl.toFloat() / summary.waterGoalMl).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(600),
        label = "water_anim"
    )

    val calorieProgress by animateFloatAsState(
        targetValue = if (summary.calorieGoal > 0) (summary.caloriesConsumedToday.toFloat() / summary.calorieGoal).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(600),
        label = "calorie_anim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Water Intake Module ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aestheticContainer(
                    aesthetic = themeConfig.aesthetic,
                    glowAccent = glowAccent,
                    hasGlow = false,
                    cornerRadius = density.cornerRadius
                )
                .padding(density.cardPadding)
                .testTag("card_water_tracker")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Water",
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hydration Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${summary.waterConsumedTodayMl} / ${summary.waterGoalMl} ml (${(waterProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF06B6D4)
                            )
                        }
                    }

                    // Quick Log Buttons (+250ml, +500ml)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onLogWater(250) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4).copy(alpha = 0.18f), contentColor = Color(0xFF06B6D4)),
                            modifier = Modifier.testTag("btn_log_water_250")
                        ) {
                            Text("+250ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onLogWater(500) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4), contentColor = Color.White),
                            modifier = Modifier.testTag("btn_log_water_500")
                        ) {
                            Text("+500ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Linear Water Progress Bar
                LinearProgressIndicator(
                    progress = { waterProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF06B6D4),
                    trackColor = Color(0xFF06B6D4).copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )

                if (waterLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(waterLogs) { log ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF06B6D4).copy(alpha = 0.12f),
                                border = BorderStroke(0.5.dp, Color(0xFF06B6D4).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "+${log.amountMl}ml",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete water entry",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onDeleteWater(log) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Food & Calories Module ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aestheticContainer(
                    aesthetic = themeConfig.aesthetic,
                    glowAccent = glowAccent,
                    hasGlow = false,
                    cornerRadius = density.cornerRadius
                )
                .padding(density.cardPadding)
                .testTag("card_food_tracker")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Calories",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nutrition & Calories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${summary.caloriesConsumedToday} / ${summary.calorieGoal} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    // Add Meal + AI Snap Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onOpenMealSnap,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .testTag("btn_snap_meal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "AI Calorie Snap",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = { showAddFoodDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            modifier = Modifier.testTag("btn_add_food_dialog")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Meal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Linear Calorie Progress Bar
                LinearProgressIndicator(
                    progress = { calorieProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF10B981).copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )

                // Macro summary chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🍗 Protein: ${summary.totalProteinGrams}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("🍞 Carbs: ${summary.totalCarbsGrams}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("🥑 Fat: ${summary.totalFatGrams}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Itemized Food Logs
                if (foodLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        foodLogs.forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.getMealTypeEnum().iconEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = entry.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${entry.getMealTypeEnum().displayName} • P:${entry.proteinGrams}g C:${entry.carbsGrams}g F:${entry.fatGrams}g",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${entry.calories} kcal",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { onDeleteFood(entry) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete food",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Add Food Dialog
    if (showAddFoodDialog) {
        var foodName by remember { mutableStateOf("") }
        var caloriesStr by remember { mutableStateOf("350") }
        var carbsStr by remember { mutableStateOf("30") }
        var proteinStr by remember { mutableStateOf("15") }
        var fatStr by remember { mutableStateOf("10") }
        var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }

        AlertDialog(
            onDismissRequest = { showAddFoodDialog = false },
            title = { Text("Log Food & Calories", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food / Meal Name") },
                        placeholder = { Text("e.g. Oatmeal with berries") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Meal Type selection
                    Text("Meal Type", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MealType.values()) { mType ->
                            FilterChip(
                                selected = selectedMealType == mType,
                                onClick = { selectedMealType = mType },
                                label = { Text("${mType.iconEmoji} ${mType.displayName}", fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = caloriesStr,
                            onValueChange = { caloriesStr = it },
                            label = { Text("Calories") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = proteinStr,
                            onValueChange = { proteinStr = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = carbsStr,
                            onValueChange = { carbsStr = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fatStr,
                            onValueChange = { fatStr = it },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (foodName.isNotBlank()) {
                            val entry = FoodEntry(
                                name = foodName.trim(),
                                calories = caloriesStr.toIntOrNull() ?: 200,
                                carbsGrams = carbsStr.toIntOrNull() ?: 0,
                                proteinGrams = proteinStr.toIntOrNull() ?: 0,
                                fatGrams = fatStr.toIntOrNull() ?: 0,
                                mealType = selectedMealType.name
                            )
                            onAddFood(entry)
                            showAddFoodDialog = false
                        }
                    },
                    enabled = foodName.isNotBlank()
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFoodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
