package com.myopenclaw.data.local

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedPriceAlert(
    val id: String,
    val ticker: String,
    val companyName: String = "",
    val alertType: String, // "above" or "below"
    val targetPrice: Double,
    val isEnabled: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAt: Long = 0L
)

/**
 * Simple alert storage using PreferencesManager.
 */
class AlertStore(private val preferencesManager: PreferencesManager) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getAlerts(): List<SavedPriceAlert> {
        return try {
            val data = preferencesManager.getPriceAlertsData() ?: return emptyList()
            json.decodeFromString<List<SavedPriceAlert>>(data)
        } catch (e: Exception) {
            // println("AlertStore: Failed to parse alerts: ${e.message}")
            emptyList()
        }
    }

    fun saveAlerts(alerts: List<SavedPriceAlert>) {
        try {
            preferencesManager.savePriceAlertsData(json.encodeToString(alerts))
        } catch (e: Exception) {
            // println("AlertStore: Failed to save alerts: ${e.message}")
        }
    }

    fun addAlert(alert: SavedPriceAlert) {
        val current = getAlerts().toMutableList()
        current.add(alert)
        saveAlerts(current)
    }

    fun removeAlert(id: String) {
        saveAlerts(getAlerts().filter { it.id != id })
    }

    fun toggleAlert(id: String) {
        saveAlerts(getAlerts().map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        })
    }

    fun markTriggered(id: String) {
        saveAlerts(getAlerts().map {
            if (it.id == id) it.copy(isTriggered = true, isEnabled = false) else it
        })
    }
}
