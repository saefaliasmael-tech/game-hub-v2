package com.example.watersort.core.model

data class Bottle(
    val id: Int,
    val capacity: Int = 4,
    val layers: List<LiquidColor> = emptyList()
) {
    val size: Int get() = layers.size
    val isFull: Boolean get() = layers.size >= capacity
    val isEmpty: Boolean get() = layers.isEmpty()
    val availableSpace: Int get() = (capacity - layers.size).coerceAtLeast(0)

    val topColor: LiquidColor? get() = layers.lastOrNull()

    /**
     * Number of continuous layers of the same color at the top of the bottle.
     */
    val topRunLength: Int
        get() {
            if (layers.isEmpty()) return 0
            val top = layers.last()
            var count = 0
            for (i in layers.indices.reversed()) {
                if (layers[i] == top) {
                    count++
                } else {
                    break
                }
            }
            return count
        }

    /**
     * A bottle is solved if it is completely empty or completely filled with a single color.
     */
    val isSolved: Boolean
        get() {
            if (layers.isEmpty()) return true
            if (layers.size != capacity) return false
            val first = layers.first()
            return layers.all { it == first }
        }

    /**
     * Returns true if bottle contains only one color, even if not yet full.
     */
    val isPure: Boolean
        get() {
            if (layers.isEmpty()) return true
            val first = layers.first()
            return layers.all { it == first }
        }
}
