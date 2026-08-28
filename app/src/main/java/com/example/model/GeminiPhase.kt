package com.example.model

enum class GeminiPhase(val label: String) {
    IDLE("Ready"),
    THINKING("🧠 Gemini is thinking..."),
    SEARCHING_CONTEXT("🔍 Analyzing habits, hydration & nutrition context..."),
    EXECUTING_ACTION("⚡ Executing actions & updating routines..."),
    COMPLETED("✨ Action completed"),
    ERROR("⚠️ Notice")
}
