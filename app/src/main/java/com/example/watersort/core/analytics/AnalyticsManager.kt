package com.example.watersort.core.analytics

interface AnalyticsManager {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
}

class DefaultAnalyticsManager : AnalyticsManager {
    override fun logEvent(name: String, params: Map<String, Any>) {
        // Production event logging abstraction
    }
}
