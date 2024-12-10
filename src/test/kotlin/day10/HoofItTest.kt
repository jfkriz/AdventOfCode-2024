package day10

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point

@DisplayName("Day 10 - Hoof It")
@TestMethodOrder(OrderAnnotation::class)
class HoofItTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 36`() {
        assertEquals(36, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 81`() {
        assertEquals(81, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 607`() {
        assertEquals(607, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1384`() {
        assertEquals(1384, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val grid = data.map { it.map { n -> n.digitToInt() } }
    private val trailHeads =
        grid
            .mapIndexed { y, row ->
                row.mapIndexedNotNull { x, value ->
                    if (value == 0) {
                        Point(x, y)
                    } else {
                        null
                    }
                }
            }.flatten()
            .toList()

    operator fun List<List<Int>>.contains(point: Point): Boolean = point.y in this.indices && point.x in this.first().indices

    operator fun List<List<Int>>.get(value: Point): Int = this[value.y][value.x]

    fun solvePartOne(): Int = trailHeads.sumOf { calculateTrailheadScore(it) }

    private fun calculateTrailheadScore(
        trailhead: Point,
        calculateRating: Boolean = false,
    ): Int {
        // A valid trail is one that starts with the trailhead and visits neighboring points with values increasing by 1 at each step.
        // Neighboring points are vertical and horizontal only, diagonals are not allowed.
        // A trail will thus be a list of points such that the values are 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 in that order.
        // A trailhead's score is the number of distinct "9" values that can be reached starting at the trailhead (part 1)
        // or the number of ways to reach any of the possible peaks (part 2).
        val visited = mutableSetOf<Point>()
        val peaks = mutableSetOf<Point>()
        val queue = mutableListOf(trailhead)
        var rating = 0
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            visited.add(current)
            val currentVal = grid[current]

            // If we have reached the peak, we can stop this path on the trailhead.
            if (currentVal == 9) {
                // For part 1, we need to calculate the score of the trailhead, which is the number of distinct peak values that can be reached.
                peaks.add(current)

                // For part 2, we need to calculate the rating of the trailhead, which is the number of ways a trail can get to any peak
                rating++

                continue
            }

            // Get all the neighbors of the current point that are valid and have not been visited.
            val neighbors =
                current.neighbors
                    .asSequence()
                    .filterNot { it.key.diagonal }
                    .map { it.value }
                    .filter { it in grid }
                    .filterNot { it in visited }
                    .filter { grid[it] == currentVal + 1 }
                    .toList()
            queue.addAll(neighbors)
        }

        return if (calculateRating) {
            rating
        } else {
            peaks.size
        }
    }

    fun solvePartTwo(): Int = trailHeads.sumOf { calculateTrailheadScore(it, true) }
}
