package day19

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 19 - Linen Layout")
@TestMethodOrder(OrderAnnotation::class)
class LinenLayoutTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 6`() {
        assertEquals(6, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 16`() {
        assertEquals(16, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 209`() {
        assertEquals(209, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 777669668613191`() {
        assertEquals(777669668613191, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val patterns: List<String> = data[0].split(",").map { it.trim() }
    private val designs: List<String> = data.drop(2)

    fun solvePartOne(): Int {
        val found = mutableMapOf("" to 1L)
        return designs.count { findPermutations(it, patterns, found) > 0L }
    }

    fun solvePartTwo(): Long {
        val found = mutableMapOf("" to 1L)
        return designs.sumOf { findPermutations(it, patterns, found) }
    }

    /**
     * Recursively find all permutations of the design by looking for patterns that match the start of the design,
     * then remove the pattern from the start of the design and recursively find all permutations of the remaining
     * design after removing the pattern.
     */
    private fun findPermutations(
        design: String,
        patterns: List<String>,
        found: MutableMap<String, Long>,
    ): Long {
        found[design]?.let { return it }

        found[design] =
            patterns
                .filter { design.startsWith(it) }
                .sumOf { pattern -> findPermutations(design.removePrefix(pattern), patterns, found) }
        return found[design]!!
    }
}
