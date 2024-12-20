package day20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point
import util.extensions.contains
import util.extensions.get

@DisplayName("Day 20 - Race Condition")
@TestMethodOrder(OrderAnnotation::class)
class RaceConditionTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 44`() {
        assertEquals(44, sampleSolver.solvePartOne(2))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 285`() {
        assertEquals(285, sampleSolver.solvePartTwo(50))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 1327`() {
        assertEquals(1327, solver.solvePartOne(100))
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 985737`() {
        assertEquals(985737, solver.solvePartTwo(100))
    }
}

class Solver(
    data: List<String>,
) {
    private val raceTrack = RaceTrack.fromInput(data)

    fun solvePartOne(minSavings: Int): Int = raceTrack.findCheatPaths(2, minSavings)

    fun solvePartTwo(minSavings: Int): Int = raceTrack.findCheatPaths(20, minSavings)
}

data class RaceTrack(
    val track: List<List<Char>>,
    val path: List<Point>,
    val start: Point,
    val end: Point,
) {
    fun findCheatPaths(
        maxToCheat: Int,
        minSavings: Int,
    ): Int {
        val savingsMap = mutableMapOf<Int, Int>()
        (0 until path.lastIndex - 1).sumOf { i ->
            (i + 1 until path.size).count { j ->
                val distToCheat = path[i].distanceFrom(path[j])
                val found = (distToCheat <= maxToCheat) && ((j - i - distToCheat) >= minSavings)
                if (found) {
                    savingsMap[j - i - distToCheat] = (savingsMap[j - i - distToCheat] ?: 0) + 1
                }
                found
            }
        }
        return savingsMap.values.sum()
    }

    companion object {
        fun fromInput(input: List<String>): RaceTrack {
            val track = input.map { it.toList() }
            val start =
                track
                    .mapIndexed { y, chars ->
                        chars.mapIndexedNotNull { x, c ->
                            if (c == 'S') Point(x, y) else null
                        }
                    }.flatten()
                    .firstOrNull() ?: throw IllegalArgumentException("No start found")
            val end =
                track
                    .mapIndexed { y, chars ->
                        chars.mapIndexedNotNull { x, c ->
                            if (c == 'E') Point(x, y) else null
                        }
                    }.flatten()
                    .firstOrNull() ?: throw IllegalArgumentException("No end found")
            val path = mutableListOf(start)
            val visited = mutableSetOf<Point>()
            var next: Point = start
            while (next != end) {
                visited.add(next)
                next =
                    next
                        .neighbors
                        .filterNot { it.key.diagonal }
                        .map { it.value }
                        .firstOrNull { it !in visited && it in track && track[it] != '#' }
                        ?: throw IllegalArgumentException("No path found")
                path.add(next)
            }
            return RaceTrack(track, path, start, end)
        }
    }
}
