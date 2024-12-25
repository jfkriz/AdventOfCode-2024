package day25

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 25 - Code Chronicle")
@TestMethodOrder(OrderAnnotation::class)
class CodeChronicleTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 3`() {
        assertEquals(3, sampleSolver.solvePartOne())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 3365`() {
        assertEquals(3365, solver.solvePartOne())
    }
}

class Solver(
    data: List<String>,
) {
    private val schematics = data.chunked().map { Schematic(it.map { line -> line.toList() }) }

    fun solvePartOne(): Int =
        schematics.filter { it.key }.sumOf { key ->
            schematics.filter { it.lock }.count { lock -> key.fits(lock) }
        }
}

data class Schematic(
    val drawing: List<List<Char>>,
) {
    val lock: Boolean
        get() = drawing[0][0] == '#'
    val key: Boolean
        get() = !lock
    private val pins: List<Int> by lazy {
        (if (lock) drawing else drawing.reversed())
            .drop(1)
            .fold(Array(drawing[0].size) { 0 }) { acc, row ->
                row.forEachIndexed { col, c ->
                    if (c == '#') acc[col]++
                }
                acc
            }.toList()
    }

    fun fits(other: Schematic): Boolean {
        if (lock == other.lock) return false
        return pins.zip(other.pins).all { (a, b) -> a + b <= 5 }
    }
}
