package day02

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.abs

@DisplayName("Day 02 - Red-Nosed Reports")
@TestMethodOrder(OrderAnnotation::class)
class RedNosedReportsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 2`() {
        assertEquals(2, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 4`() {
        assertEquals(4, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 564`() {
        assertEquals(564, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 604`() {
        assertEquals(604, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val reports: List<Report> = data.map { Report(it) }

    fun solvePartOne(): Int {
        return reports.map(Report::isSafe).count { it }
    }

    fun solvePartTwo(): Int {
        return reports.map(Report::isSafeWithDampening).count { it }
    }
}

data class Report(private val levels: List<Int>) {
    constructor(input: String) : this(input.split(" ").map { it.toInt() })

    /**
     * Return true if the levels are either gradually increasing, or gradually decreasing. Gradually in both
     * contexts means that the numbers are either increasing or decreasing by not more than 3.
     */
    fun isSafe(): Boolean {
        val deltas = levels.zipWithNext().map { (a, b) -> b - a }

        if (!deltas.all { it > 0 } && !deltas.all { it < 0 }) {
            return false
        }

        return deltas.map { abs(it) }.all { it <= 3 }
    }

    /**
     * Return true if isSafe is true, or if you can remove one number from the list and then have a safe list.
     * This solution is not very elegant, but surprisingly, the brute force approach is fast enough.
     */
    fun isSafeWithDampening(): Boolean {
        return isSafe() ||
            levels.indices.any { index ->
                val dampened = levels.toMutableList()
                dampened.removeAt(index)
                Report(dampened).isSafe()
            }
    }
}
