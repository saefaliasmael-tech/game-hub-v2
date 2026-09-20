package com.example.watersort.core.engine

import com.example.watersort.core.model.PourDelta

class UndoStack {
    private val stack = ArrayDeque<PourDelta>()

    val size: Int get() = stack.size
    val canUndo: Boolean get() = stack.isNotEmpty()

    fun push(delta: PourDelta) {
        stack.addLast(delta)
    }

    fun peekLast(): PourDelta? {
        return stack.lastOrNull()
    }

    fun pop(): PourDelta? {
        return if (stack.isNotEmpty()) stack.removeLast() else null
    }

    fun clear() {
        stack.clear()
    }

    fun toList(): List<PourDelta> = stack.toList()

    fun restore(deltas: List<PourDelta>) {
        stack.clear()
        stack.addAll(deltas)
    }
}
