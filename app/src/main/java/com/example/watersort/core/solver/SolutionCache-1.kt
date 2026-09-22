package com.example.watersort.core.solver

import com.example.watersort.core.model.PourMove
import java.util.Collections
import java.util.LinkedHashMap

object SolutionCache {
    private const val MAX_ENTRIES = 500

    private val cache: MutableMap<String, List<PourMove>> = Collections.synchronizedMap(
        object : LinkedHashMap<String, List<PourMove>>(MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<PourMove>>?): Boolean {
                return size > MAX_ENTRIES
            }
        }
    )

    fun put(key: String, solution: List<PourMove>) {
        cache[key] = solution
    }

    fun get(key: String): List<PourMove>? = cache[key]

    fun clear() {
        cache.clear()
    }
}

