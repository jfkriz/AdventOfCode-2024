package day18

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
import util.extensions.getOrDefault
import util.extensions.set
import kotlin.math.min

@DisplayName("Day 18 - RAM Run")
@TestMethodOrder(OrderAnnotation::class)
class RamRunTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 22`() {
        assertEquals(22, sampleSolver.solvePartOne(7, 7, 12))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 6,1`() {
        assertEquals("6,1", sampleSolver.solvePartTwo(7, 7, 12))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 252`() {
        assertEquals(252, solver.solvePartOne(71, 71, 1024))
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 5,60`() {
        assertEquals("5,60", solver.solvePartTwo(71, 71, 1024))
    }
}

class Solver(
    data: List<String>,
) {
    private val byteLocations = data.map { it.split(",") }.map { Point(it[0].toInt(), it[1].toInt()) }

    fun solvePartOne(
        width: Int,
        height: Int,
        numBytes: Int,
    ): Int {
        val memoryMap = MemoryMap.fromInput(width, height, byteLocations, numBytes)
        return memoryMap.findShortestPath(Point(0, 0), Point(width - 1, height - 1))
    }

    fun solvePartTwo(
        width: Int,
        height: Int,
        numBytes: Int,
    ): String {
        // The part 2 solution is slow for the real input, takes about 8 seconds on my laptop.
        // I'm sure it could be optimized, but I'm happy with the solution for now, and it was
        // very simple. Just keep adding obstacles from the list of locations until we find one
        // that blocks the exit (where the path length is Int.MAX_VALUE).
        val obstacles = mutableListOf<Point>()
        val noExit =
            byteLocations.firstOrNull { obstacle ->
                val memoryMap = MemoryMap.fromInput(width, height, byteLocations, numBytes)
                obstacles.add(obstacle)
                val path = memoryMap.findShortestPath(Point(0, 0), Point(width - 1, height - 1), obstacles)
                path == Int.MAX_VALUE
            }
        return if (noExit != null) {
            "${noExit.x},${noExit.y}"
        } else {
            "No solution found"
        }
    }
}

data class MemoryMap(
    val grid: List<List<Char>>,
) {
    fun findShortestPath(
        start: Point,
        end: Point,
        obstacles: List<Point>? = null,
    ): Int {
        val queue = mutableListOf(start)
        val distances = List(grid.size) { MutableList(grid[0].size) { Int.MAX_VALUE } }
        distances[start] = 0
        val grid = grid.map { it.toMutableList() }
        obstacles?.filter { it in grid }?.forEach { grid[it] = '#' }

        while (queue.isNotEmpty()) {
            val loc = queue.removeFirst()
            queue.addAll(
                loc.neighbors
                    .asSequence()
                    .filterNot { it.key.diagonal }
                    .map { it.value }
                    .filterNot { grid.getOrDefault(it, '#') == '#' }
                    .filter { it.x <= end.x && it.y <= end.y }
                    .filter { distances[it] == Int.MAX_VALUE }
                    .map {
                        distances[it] = min(distances[it], distances[loc] + 1)
                        it
                    }.toList(),
            )
        }

        return distances[end]
    }

    companion object {
        fun fromInput(
            width: Int,
            height: Int,
            byteLocations: List<Point>,
            numBytes: Int,
        ): MemoryMap {
            // Create a 2D list with height rows and width columns, initialized with '.' in each cell
            val grid = List(height) { MutableList(width) { '.' } }
            var added = 0
            for (p in byteLocations) {
                if (p !in grid) {
                    continue
                }
                grid[p] = '#'
                added++
                if (added == numBytes) {
                    break
                }
            }
            return MemoryMap(grid)
        }
    }

    override fun toString(): String = grid.joinToString("\n") { it.joinToString("") }
}
