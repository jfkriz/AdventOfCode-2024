package day03

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 03 - Mull It Over")
@TestMethodOrder(OrderAnnotation::class)
class MullItOverTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val sampleSolver2 by lazy {
        Solver(loadOtherInput("test-input-2.txt"))
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 161`() {
        assertEquals(161, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 48`() {
        assertEquals(48, sampleSolver2.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 192767529`() {
        assertEquals(192767529, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 104083373`() {
        assertEquals(104083373, solver.solvePartTwo())
    }
}

data class Solver(
    private val data: List<String>,
) {
    private val mulRegex = Regex("mul\\((\\d+,\\d+)\\)")

    private val conditionalMulRegex = Regex("do\\(\\)|don't\\(\\)|mul\\((\\d+,\\d+)\\)")

    fun solvePartOne(): Int {
        return data.map {
            mulRegex.findAll(it)
        }.sumOf { matches ->
            matches.sumOf {
                it.groupValues[1].split(",").map { n -> n.toInt() }.reduce { acc, i -> acc * i }
            }
        }
    }

    fun solvePartTwo(): Int {
        var mulEnabled = true
        return data.map {
            conditionalMulRegex.findAll(it)
        }.sumOf { matches ->
            matches.sumOf { match ->
                when {
                    match.value == "do()" -> {
                        mulEnabled = true
                        0
                    }
                    match.value == "don't()" -> {
                        mulEnabled = false
                        0
                    }
                    match.value.startsWith("mul") && mulEnabled -> {
                        match.groupValues[1].split(",").map { n -> n.toInt() }.reduce { acc, i -> acc * i }
                    }
                    else -> 0
                }
            }
        }
    }
}
