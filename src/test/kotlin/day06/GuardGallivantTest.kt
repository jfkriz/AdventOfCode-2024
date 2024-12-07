package day06

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.Point

@DisplayName("Day 06 - Guard Gallivant")
@TestMethodOrder(OrderAnnotation::class)
class GuardGallivantTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 41`() {
        assertEquals(41, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 6`() {
        assertEquals(6, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 4580`() {
        assertEquals(4580, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1480`() {
        assertEquals(1480, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val mapGrid = data.map { it.toList() }
    private val rows = mapGrid.indices
    private val columns = mapGrid[0].indices

    // Locate the starting position on the grid
    private val startingPosition =
        mapGrid
            .flatMapIndexed { y, row ->
                row.mapIndexedNotNull { x, cell ->
                    if (GuardDirection.fromIcon(cell) != null) Point(x, y) else null
                }
            }.first()

    fun solvePartOne(): Int {
        var current = startingPosition
        var direction = GuardDirection.fromIcon(mapGrid[current.y][current.x]) ?: error("Invalid starting direction")
        val pointsVisited = mutableSetOf<Point>()
        pointsVisited.add(current.copy())

        while (true) {
            val nextPosition = current.move(direction.heading)

            // If we're exiting the grid, then we're done.
            if (nextPosition.x !in columns || nextPosition.y !in rows) {
                break
            }

            // If we're hitting an object, then we need to turn right.
            if (mapGrid[nextPosition.y][nextPosition.x] == '#') {
                direction = direction.turnRight()
                continue
            }

            // Take a step in the current direction.
            current = nextPosition
            pointsVisited.add(current.copy())
        }
        return pointsVisited.size
    }

    /**
     * Note: This solution is not optimized and takes about 6 seconds to run on my input.
     */
    fun solvePartTwo(): Int {
        val possibleObstructionPositions = mutableSetOf<Point>()

        for (y in rows) {
            for (x in columns) {
                val point = Point(x, y)
                if (point == startingPosition || mapGrid[y][x] == '#') continue

                if (causesLoopWithObstruction(point)) {
                    possibleObstructionPositions.add(point)
                }
            }
        }

        return possibleObstructionPositions.size
    }

    private fun causesLoopWithObstruction(obstruction: Point): Boolean {
        var current = startingPosition
        var direction = GuardDirection.fromIcon(mapGrid[current.y][current.x]) ?: error("Invalid starting direction")
        val visited = mutableSetOf<Pair<Point, GuardDirection>>()
        visited.add(current to direction)

        while (true) {
            val nextPosition = current.move(direction.heading)

            // If we're exiting the grid, then we're not in a loop
            if (nextPosition.x !in columns || nextPosition.y !in rows) {
                return false
            }

            // If we're hitting an object or the new obstruction, then we need to turn right.
            if (mapGrid[nextPosition.y][nextPosition.x] == '#' || nextPosition == obstruction) {
                direction = direction.turnRight()
                continue
            }

            // If we revisit a position with the same direction, it means we're in a loop.
            if (!visited.add(nextPosition to direction)) {
                return true
            }

            // Take a step in the current direction.
            current = nextPosition
        }
    }
}

enum class GuardDirection(
    val icon: Char,
    val heading: Direction,
) {
    UP('^', Direction.Up),
    DOWN('v', Direction.Down),
    LEFT('<', Direction.Left),
    RIGHT('>', Direction.Right),
    ;

    companion object {
        fun fromIcon(icon: Char) = entries.firstOrNull { it.icon == icon }
    }

    fun turnRight() =
        when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }
}
