package day22

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.truncate

@DisplayName("Day 22 - Monkey Market")
@TestMethodOrder(OrderAnnotation::class)
class MonkeyMarketTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val sampleSolverPart2 by lazy {
        Solver(loadOtherInput("test-input-part2.txt"))
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 37327623`() {
        assertEquals(37327623, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 23`() {
        assertEquals(23, sampleSolverPart2.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 13753970725`() {
        assertEquals(13753970725, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1570`() {
        assertEquals(1570, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val initialSecretNumbers = data.map { it.toLong() }

    fun solvePartOne(): Long = initialSecretNumbers.sumOf { generateSecretNumber(it) }

    fun solvePartTwo(): Int {
        val bestPrices = mutableMapOf<List<Int>, Int>()

        initialSecretNumbers.map { generatePriceChanges(it) }.forEach { buyer ->
            val seenChanges = HashSet<List<Int>>()
            buyer.windowed(4).forEach { window ->
                val key = window.map { it.change }
                if (seenChanges.add(key)) {
                    bestPrices[key] = bestPrices.getOrDefault(key, 0) + window.last().price
                }
            }
        }
        return bestPrices.maxOf { it.value }
    }

    private fun generateSecretNumber(initial: Long): Long {
        var current = initial
        repeat(2000) {
            current = current.step1().step2().step3()
        }
        return current
    }

    private fun generatePriceChanges(initial: Long): List<PriceChange> {
        var current = initial
        val changes =
            (0 until 2000).map {
                val next = current.step1().step2().step3()
                val diff = PriceChange((next % 10).toInt(), (next % 10).toInt() - (current % 10).toInt())
                current = next
                diff
            }
        return changes
    }

    private fun Long.step1(): Long = (this * 64).mix(this).prune()

    private fun Long.step2(): Long = truncate(this / 32.0).toLong().mix(this).prune()

    private fun Long.step3(): Long = (this * 2048).mix(this).prune()

    private fun Long.mix(other: Long): Long = this.xor(other)

    private fun Long.prune(): Long = this % 16777216
}

data class PriceChange(
    val price: Int,
    val change: Int,
)
