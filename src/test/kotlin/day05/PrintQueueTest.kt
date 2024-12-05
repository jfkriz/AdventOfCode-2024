package day05

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 05 - Print Queue")
@TestMethodOrder(OrderAnnotation::class)
class PrintQueueTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 143`() {
        assertEquals(143, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 123`() {
        assertEquals(123, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 4185`() {
        assertEquals(4185, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 4480`() {
        assertEquals(4480, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val rules = data.chunked()[0].map { it.split("|").map { n -> n.toInt() } }
    private val updates = data.chunked()[1].map { it.split(",").map { n -> n.toInt() } }
    private val ruleComparator =
        Comparator<Int> { a, b ->
            // Find the rule that includes both a and b - this assumes there are no cycles in the rules!
            val rule = rules.find { it.contains(a) && it.contains(b) }
            if (rule == null) {
                0 // No rule applies, a and b are considered equal
            } else {
                if (a == rule[0]) -1 else 1 // a comes before b if it matches the first element of the rule
            }
        }

    fun solvePartOne(): Int {
        val orderedUpdates = updates.filter { isUpdateInProperOrder(it) }

        return orderedUpdates.sumOf { it[it.size / 2] }
    }

    private fun isUpdateInProperOrder(update: List<Int>): Boolean {
        val ordered = update.sortedWith(ruleComparator)
        return update == ordered
    }

    fun solvePartTwo(): Int {
        val unorderedUpdates = updates.filter { !isUpdateInProperOrder(it) }

        val ordered = unorderedUpdates.map { it.sortedWith(ruleComparator) }

        return ordered.sumOf { it[it.size / 2] }
    }
}
