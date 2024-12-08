package day08

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point
import util.extensions.combinations

@DisplayName("Day 08 - Resonant Collinearity")
@TestMethodOrder(OrderAnnotation::class)
class ResonantCollinearityTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 14`() {
        assertEquals(14, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 34`() {
        assertEquals(34, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 369`() {
        assertEquals(369, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1169`() {
        assertEquals(1169, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val antennas =
        data
            .mapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    if (char != '.') char to Point(x, y) else null
                }
            }.flatten()
            .groupBy({ it.first }, { it.second })
            .toMap()
    private var rows = data.indices
    private var cols = data[0].indices

    fun solvePartOne(): Int =
        antennas.values
            .flatMap {
                it.asSequence().combinations(2).flatMap { (ant1, ant2) -> findAntiNodesForPair(ant1, ant2) }
            }.toSet()
            .size

    private fun findAntiNodesForPair(
        ant1: Point,
        ant2: Point,
    ): List<Point> =
        listOf(
            ant1 + (ant2 - ant1) * 2,
            ant2 + (ant1 - ant2) * 2,
        ).filter { it.x in cols && it.y in rows }

    fun solvePartTwo(): Int =
        antennas.values
            .flatMap {
                it.asSequence().combinations(2).flatMap { (ant1, ant2) -> findAllAntiNodesForPair(ant1, ant2) }
            }.toSet()
            .size

    private fun findAllAntiNodesForPair(
        ant1: Point,
        ant2: Point,
    ): List<Point> {
        val antiNodes = mutableListOf<Point>()
        var current = ant1
        while (current.x in cols && current.y in rows) {
            antiNodes.add(current)
            current += ant1 - ant2
        }
        current = ant2
        while (current.x in cols && current.y in rows) {
            antiNodes.add(current)
            current += ant2 - ant1
        }
        return antiNodes
    }
}
