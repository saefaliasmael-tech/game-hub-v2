package com.example.iqboost.engine

import androidx.compose.ui.graphics.Color
import com.example.iqboost.model.*
import kotlin.random.Random

class IQBoostEngine {
    fun generateMemoryMatrix(round: Int): Set<Int> {
        val count = 4 + (round / 2).coerceAtMost(3)
        val selected = mutableSetOf<Int>()
        val random = Random.Default
        while (selected.size < count) {
            selected.add(random.nextInt(16))
        }
        return selected
    }

    fun generateSpeedMath(): SpeedMathQuestion {
        val random = Random.Default
        val op = random.nextInt(3)
        val a: Int
        val b: Int
        val ans: Int
        val eq: String

        when (op) {
            0 -> { // Addition
                a = random.nextInt(15, 60)
                b = random.nextInt(12, 50)
                ans = a + b
                eq = "$a + $b = ?"
            }
            1 -> { // Subtraction
                a = random.nextInt(30, 99)
                b = random.nextInt(10, a)
                ans = a - b
                eq = "$a - $b = ?"
            }
            else -> { // Multiplication
                a = random.nextInt(4, 12)
                b = random.nextInt(4, 12)
                ans = a * b
                eq = "$a × $b = ?"
            }
        }

        val options = mutableListOf(ans)
        while (options.size < 4) {
            val offset = (random.nextInt(1, 6)) * if (random.nextBoolean()) 1 else -1
            val fake = ans + offset
            if (fake > 0 && !options.contains(fake)) {
                options.add(fake)
            }
        }
        options.shuffle()
        return SpeedMathQuestion(
            equation = eq,
            options = options,
            correctIndex = options.indexOf(ans)
        )
    }

    fun generateStroop(): StroopQuestion {
        val colorNames = listOf("RED", "BLUE", "GREEN", "YELLOW")
        val colorMap = mapOf(
            "RED" to Color(0xFFFF1744),
            "BLUE" to Color(0xFF2979FF),
            "GREEN" to Color(0xFF00E676),
            "YELLOW" to Color(0xFFFFEA00)
        )

        val random = Random.Default
        val word = colorNames[random.nextInt(colorNames.size)]
        val isMatch = random.nextBoolean()
        val textColorName = if (isMatch) word else colorNames.filter { it != word }[random.nextInt(colorNames.size - 1)]

        return StroopQuestion(
            wordText = word,
            textColorName = textColorName,
            colorValue = colorMap[textColorName] ?: Color.White,
            isMatch = isMatch
        )
    }

    fun generatePattern(): NumberPatternQuestion {
        val random = Random.Default
        val type = random.nextInt(3)
        val seq: List<Int>
        val answer: Int

        when (type) {
            0 -> { // Add step
                val start = random.nextInt(2, 10)
                val step = random.nextInt(3, 8)
                seq = listOf(start, start + step, start + step * 2, start + step * 3)
                answer = start + step * 4
            }
            1 -> { // Multiply by 2 or 3
                val factor = if (random.nextBoolean()) 2 else 3
                val start = random.nextInt(1, 4)
                seq = listOf(start, start * factor, start * factor * factor, start * factor * factor * factor)
                answer = start * factor * factor * factor * factor
            }
            else -> { // Squares
                val start = random.nextInt(1, 4)
                seq = (start until start + 4).map { it * it }
                answer = (start + 4) * (start + 4)
            }
        }

        val options = mutableListOf(answer)
        while (options.size < 4) {
            val fake = answer + (random.nextInt(1, 7)) * if (random.nextBoolean()) 1 else -1
            if (fake > 0 && !options.contains(fake)) {
                options.add(fake)
            }
        }
        options.shuffle()

        return NumberPatternQuestion(
            sequence = seq.joinToString(", ") + ", [ ? ]",
            options = options,
            correctIndex = options.indexOf(answer)
        )
    }
}
