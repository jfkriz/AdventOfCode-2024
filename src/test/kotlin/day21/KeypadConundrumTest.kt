package day21

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point

@DisplayName("Day 21 - Keypad Conundrum")
@TestMethodOrder(OrderAnnotation::class)
class KeypadConundrumTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 126384`() {
        assertEquals(126384, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 154115708116294`() {
        assertEquals(154115708116294, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 152942`() {
        assertEquals(152942, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 189235298434780`() {
        assertEquals(189235298434780, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val codes = data.map { KeypadCode(it) }

    fun solvePartOne(): Long =
        codes.sumOf {
            movements(0, it.code, 3) * it.number
        }

    private fun movements(
        currentLevel: Int,
        sequence: String,
        totalLevels: Int = 3,
        cache: Array<MutableMap<String, Long>> = Array(totalLevels) { mutableMapOf() },
    ): Long {
        if (currentLevel == totalLevels) return sequence.length.toLong()
        cache[currentLevel][sequence]?.let { return it }
        if (sequence.isEmpty()) return 0

        val pad =
            if (currentLevel == 0) {
                KeyPad.entries.associate { it.digit to Pair(it.position.y, it.position.x) }
            } else {
                DPad.entries.associate { it.direction to Pair(it.position.y, it.position.x) }
            }

        var buttonCount = 0L
        var currentChar = 'A'

        for (cur in sequence) {
            val (x1, y1) = pad[currentChar] ?: continue
            val (x2, y2) = pad[cur] ?: continue
            val (xn, yn) = pad[' '] ?: continue

            var movementNum = Long.MAX_VALUE

            val s1 = "^".repeat((x1 - x2).coerceAtLeast(0)) + "v".repeat((x2 - x1).coerceAtLeast(0))
            val s2 = "<".repeat((y1 - y2).coerceAtLeast(0)) + ">".repeat((y2 - y1).coerceAtLeast(0))

            if (x1 != xn || y2 != yn) {
                movementNum = movementNum.coerceAtMost(movements(currentLevel + 1, "$s2${s1}A", totalLevels, cache))
            }
            if (x2 != xn || y1 != yn) {
                movementNum = movementNum.coerceAtMost(movements(currentLevel + 1, "$s1${s2}A", totalLevels, cache))
            }

            buttonCount += movementNum
            currentChar = cur
        }
        cache[currentLevel][sequence] = buttonCount
        return buttonCount
    }

    fun solvePartTwo(): Long =
        codes.sumOf {
            movements(0, it.code, 26) * it.number
        }
}

enum class KeyPad(
    val digit: Char,
    val position: Point,
) {
    SEVEN('7', Point(0, 0)),
    EIGHT('8', Point(1, 0)),
    NINE('9', Point(2, 0)),
    FOUR('4', Point(0, 1)),
    FIVE('5', Point(1, 1)),
    SIX('6', Point(2, 1)),
    ONE('1', Point(0, 2)),
    TWO('2', Point(1, 2)),
    THREE('3', Point(2, 2)),
    EMPTY(' ', Point(0, 3)),
    ZERO('0', Point(1, 3)),
    ACTIVATE('A', Point(2, 3)),
}

enum class DPad(
    val direction: Char,
    val position: Point,
) {
    BLANK(' ', Point(0, 0)),
    UP('^', Point(1, 0)),
    ACTIVATE('A', Point(2, 0)),
    LEFT('<', Point(0, 1)),
    DOWN('v', Point(1, 1)),
    RIGHT('>', Point(2, 1)),
}

data class KeypadCode(
    val code: String,
) {
    val number: Int = code.dropLast(1).trimStart('0').toInt()
}
