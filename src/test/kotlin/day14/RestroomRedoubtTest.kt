package day14

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point

@DisplayName("Day 14 - Restroom Redoubt")
@TestMethodOrder(OrderAnnotation::class)
class RestroomRedoubtTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 12`() {
        assertEquals(12, sampleSolver.solvePartOne(11, 7))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 228410028`() {
        assertEquals(228410028, solver.solvePartOne(101, 103))
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 8258`() {
        assertEquals(8258, solver.solvePartTwo(101, 103))
    }
}

class Solver(
    data: List<String>,
) {
    private val robotMap = RobotMap.fromStrings(data)

    fun solvePartOne(
        maxX: Int,
        maxY: Int,
    ): Int =
        robotMap
            .move(100, maxX to maxY)
            .quadrants(maxX to maxY)
            .map { it.size }
            .reduce { acc, i -> acc * i }

    /**
     * This is a hack, and may not work for all inputs. I'm sure there is a better way to determine if there
     * is a tree rendered by the robots on the grid, but I first set this up to run 10000 iterations, and for
     * each iteration, I printed out the iteration number and the rendering of the robots. Once that was done,
     * I just manually inspected the output (I sent it to a file), and looked for a pattern that looked like a
     * tree. I noticed that the tree had a boundary box of 31 horizontal characters, so I updated this code
     * to break out of the loop when the boundary box was detected. This is not a good solution, but it works for
     * my input, and could probably use the same general idea to work for other inputs. A more general way to do this might
     * be to look for a grid where there are a large number of contiguous "#" characters, which would hopefully
     * only happen when a tree has been rendered.
     */
    fun solvePartTwo(
        maxX: Int,
        maxY: Int,
    ): Int {
        val boundaryBox = "#".repeat(31)
        var iteration = 0
        var next = robotMap.copy()
        while (true) {
            iteration++
            next = next.move(1, maxX to maxY)
            val possibleTree = next.drawTreeMap(maxX to maxY)
            if (possibleTree.contains(boundaryBox)) {
                println()
                println("Iteration: $iteration")
                println(possibleTree)
                break
            }
        }
        return iteration
    }
}

data class RobotMap(
    val robots: List<Robot>,
) {
    fun move(
        iterations: Int,
        maxXY: Pair<Int, Int>,
    ): RobotMap = RobotMap(robots.map { it.move(iterations, maxXY) })

    fun quadrants(maxXY: Pair<Int, Int>): List<List<Robot>> {
        val halfX = maxXY.first / 2
        val halfY = maxXY.second / 2
        val quadrants =
            robots
                .groupBy {
                    when {
                        it.startingPosition.x < halfX && it.startingPosition.y < halfY -> 0
                        it.startingPosition.x > halfX && it.startingPosition.y < halfY -> 1
                        it.startingPosition.x < halfX && it.startingPosition.y > halfY -> 2
                        it.startingPosition.x > halfX && it.startingPosition.y > halfY -> 3
                        else -> -1
                    }
                }
        return quadrants
            .filterNot { it.key == -1 }
            .toSortedMap()
            .values
            .toList()
    }

    companion object {
        fun fromStrings(input: List<String>): RobotMap = RobotMap(input.map { Robot.fromString(it) })
    }

    fun drawTreeMap(maxXY: Pair<Int, Int>): String {
        val map = Array(maxXY.second) { Array(maxXY.first) { "." } }
        robots.forEach { robot ->
            map[robot.startingPosition.y][robot.startingPosition.x] = "#"
        }
        return map.joinToString("\n") { it.joinToString("") }
    }
}

data class Robot(
    val startingPosition: Point,
    val velocity: Point,
) {
    fun move(
        iterations: Int,
        maxXY: Pair<Int, Int>,
    ): Robot {
        val x = ((startingPosition.x + velocity.x * iterations) % maxXY.first + maxXY.first) % maxXY.first
        val y = ((startingPosition.y + velocity.y * iterations) % maxXY.second + maxXY.second) % maxXY.second

        return copy(startingPosition = Point(x, y))
    }

    companion object {
        private val regex = Regex("""p=(-?\d+),(-?\d+)\s+v=(-?\d+),(-?\d+)""")

        fun fromString(input: String): Robot {
            val match = regex.matchEntire(input) ?: throw IllegalArgumentException("Invalid input: $input")
            val (x, y, vX, vY) = match.destructured.toList().map { it.toInt() }
            return Robot(Point(x, y), Point(vX, vY))
        }
    }
}
