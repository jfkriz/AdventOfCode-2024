package day01

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.abs

@DisplayName("Day 01 - Historian Hysteria")
@TestMethodOrder(OrderAnnotation::class)
class HistorianHysteriaTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 11`() {
        assertEquals(11, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 31`() {
        assertEquals(31, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 2375403`() {
        assertEquals(2375403, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 23082277`() {
        assertEquals(23082277, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val left = data.map { it.trim().split("\\s+".toRegex())[0].toInt() }
    private val right = data.map { it.trim().split("\\s+".toRegex())[1].toInt() }

    fun solvePartOne(): Int {
        val ls = left.sorted()
        val rs = right.sorted()

        val distances =
            ls.mapIndexed { index, l ->
                abs(l - rs[index])
            }
        return distances.sum()
    }

    fun solvePartTwo(): Long {
        val rightMap = right.groupingBy { it }.eachCount()

        val similarities =
            left.map {
                (rightMap[it] ?: 0) * it.toLong()
            }
        return similarities.sum()
    }
}
