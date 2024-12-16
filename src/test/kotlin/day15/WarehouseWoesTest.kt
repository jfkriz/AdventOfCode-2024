package day15

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.Point
import util.extensions.chunked
import util.extensions.contains
import util.extensions.get
import util.extensions.getOrDefault
import util.extensions.set

@DisplayName("Day 15 - Warehouse Woes")
@TestMethodOrder(OrderAnnotation::class)
class WarehouseWoesTest : DataFiles {
    private val part1SmallSampleSolver by lazy {
        Solver(loadOtherInput("small-part1-test-input.txt"))
    }
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Small Sample Input should return 2028`() {
        assertEquals(2028, part1SmallSampleSolver.solvePartOne())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 10092`() {
        assertEquals(10092, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 9021`() {
        assertEquals(9021, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 1442192`() {
        assertEquals(1442192, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1448458`() {
        assertEquals(1448458, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val warehouse = Warehouse.fromInput(data)

    private val wideWarehouse = WideWarehouse.fromInput(data)

    fun solvePartOne(): Int = warehouse.executeMoves().calculateBoxGpsCoordinates().sum()

    fun solvePartTwo(): Int = wideWarehouse.executeMoves().calculateBoxGpsCoordinates().sum()
}

data class WideWarehouse(
    val grid: List<List<Icon>>,
    val robot: Point,
    val moves: List<Direction>,
) {
    companion object {
        fun fromInput(data: List<String>): WideWarehouse {
            val (map, instructions) = data.chunked()
            var robot: Point? = null
            val grid =
                map
                    .mapIndexed { y, row ->
                        row
                            .toCharArray()
                            .mapIndexed { x, ch ->
                                when (val icon = Icon.fromChar(ch)) {
                                    Icon.ROBOT -> {
                                        robot = Point(x * 2, y)
                                        listOf(Icon.OPEN, Icon.OPEN)
                                    }

                                    Icon.BOX -> {
                                        listOf(Icon.BOX_L, Icon.BOX_R)
                                    }

                                    else -> {
                                        listOf(icon, icon)
                                    }
                                }
                            }.flatten()
                            .toList()
                    }

            if (robot == null) {
                throw IllegalArgumentException("No robot found in grid")
            }

            val moves = instructions.joinToString("").map { Direction.fromChar(it) }

            return WideWarehouse(grid, robot!!, moves)
        }
    }

    fun calculateBoxGpsCoordinates(): List<Int> {
        val boxCoordinates = mutableListOf<Int>()
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                if (grid[y][x] == Icon.BOX_L) {
                    boxCoordinates.add((100 * y) + x)
                }
            }
        }
        return boxCoordinates
    }

    private fun canMoveBoxesVertically(
        newPosition: Point,
        direction: Direction,
        grid: List<List<Icon>>,
    ): Boolean {
        val (newLeft, newRight) =
            if (grid[newPosition] == Icon.BOX_L) {
                newPosition.move(direction) to newPosition.move(Direction.Right).move(direction)
            } else {
                newPosition.move(Direction.Left).move(direction) to newPosition.move(direction)
            }

        return (
            grid[newLeft] == Icon.OPEN ||
                (
                    grid[newLeft].isBox &&
                        canMoveBoxesVertically(
                            newLeft,
                            direction,
                            grid,
                        )
                )
        ) &&
            (
                grid[newRight] == Icon.OPEN ||
                    (
                        grid[newRight].isBox &&
                            canMoveBoxesVertically(
                                newRight,
                                direction,
                                grid,
                            )
                    )
            )
    }

    private fun moveBoxesVertically(
        newPosition: Point,
        direction: Direction,
        grid: List<MutableList<Icon>>,
    ) {
        val (left, right) =
            if (grid[newPosition] == Icon.BOX_L) {
                newPosition to newPosition.move(Direction.Right)
            } else {
                newPosition.move(Direction.Left) to newPosition
            }

        val newLeft = left.move(direction)
        val newRight = right.move(direction)

        if (grid[newLeft].isBox) moveBoxesVertically(newLeft, direction, grid)
        if (grid[newRight].isBox) moveBoxesVertically(newRight, direction, grid)
        grid[newLeft] = grid[left]
        grid[newRight] = grid[right]
        grid[left] = Icon.OPEN
        grid[right] = Icon.OPEN
    }

    fun executeMoves(): WideWarehouse {
        val newGrid = grid.map { it.toMutableList() }
        var robot = robot
        for (move in moves) {
            val newPosition = robot.move(move)

            // If it is outside the grid, or we hit a wall, skip this move
            if (newGrid.getOrDefault(newPosition, Icon.WALL) == Icon.WALL) {
                continue
            }

            // Move the robot if there is an open space
            if (newGrid[newPosition] == Icon.OPEN) {
                newGrid[newPosition] = Icon.OPEN
                newGrid[robot] = Icon.OPEN
                robot = newPosition
                continue
            }

            // This has to be a box, so first see if we are moving horizontally - this is a relatively easy move
            if (move == Direction.Left || move == Direction.Right) {
                var next = newPosition
                // Iterate in `direction` while there are boxes in the way
                while (next in newGrid && newGrid[next].isBox) {
                    next = next.move(move)
                }
                // If the next position puts us off the grid or is not open, skip this move
                if (newGrid.getOrDefault(next, Icon.WALL) != Icon.OPEN) continue

                // I should be able to do some array slicing here to basically shift the
                // entire chunk of boxen left or right, but I was not getting that to work,
                // so this is a more brute force iterative approach. This will just move the
                // box sides one at a time, working from the open space back to the robot.
                // For instance, if this robot needs to ove right, and the grid looks like this:
                // @[][][]....
                // We will iterate like this:
                // @[][][.]...
                // @[][].[]...
                // @[][.][]...
                // ..and so on, until...
                // @.[][][]...
                val prevDir = if (move == Direction.Left) Direction.Right else Direction.Left
                while (next != newPosition) {
                    val prev = next.move(prevDir)
                    newGrid[next] = newGrid[prev]
                    next = prev
                }
                newGrid[newPosition] = Icon.OPEN
                newGrid[robot] = Icon.OPEN
                robot = newPosition
                continue
            }

            // Must be a vertical move, so handle that
            if (newGrid[newPosition].isBox && canMoveBoxesVertically(newPosition, move, newGrid)) {
                moveBoxesVertically(newPosition, move, newGrid)
                newGrid[newPosition] = Icon.OPEN
                newGrid[robot] = Icon.OPEN
                robot = newPosition
            }
        }
        return WideWarehouse(newGrid, robot, moves)
    }
}

data class Warehouse(
    val grid: List<List<Icon>>,
    val robot: Point,
    val moves: List<Direction>,
) {
    companion object {
        fun fromInput(data: List<String>): Warehouse {
            val (map, instructions) = data.chunked()
            var robot: Point? = null
            val grid =
                map
                    .mapIndexed { y, row ->
                        row
                            .toCharArray()
                            .mapIndexed { x, ch ->
                                val icon = Icon.fromChar(ch)
                                if (icon == Icon.ROBOT) {
                                    robot = Point(x, y)
                                    Icon.OPEN
                                } else {
                                    icon
                                }
                            }.toList()
                    }

            if (robot == null) {
                throw IllegalArgumentException("No robot found in grid")
            }

            val moves = instructions.joinToString("").map { Direction.fromChar(it) }

            return Warehouse(grid, robot!!, moves)
        }
    }

    fun calculateBoxGpsCoordinates(): List<Int> {
        val boxCoordinates = mutableListOf<Int>()
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                if (grid[y][x] == Icon.BOX) {
                    boxCoordinates.add((100 * y) + x)
                }
            }
        }
        return boxCoordinates
    }

    fun executeMoves(): Warehouse {
        val newGrid = grid.map { it.toMutableList() }
        var robot = robot
        for (move in moves) {
            val newPosition = robot.move(move)

            // If it is outside the grid, or we hit a wall, skip this move
            if (newGrid.getOrDefault(newPosition, Icon.WALL) == Icon.WALL) {
                continue
            }

            // Move the robot if there is an open space
            if (newGrid[newPosition] == Icon.OPEN) {
                newGrid[newPosition] = Icon.ROBOT
                newGrid[robot] = Icon.OPEN
                robot = newPosition
                continue
            }

            // Handle moving or pushing boxes
            if (newGrid[newPosition] == Icon.BOX) {
                // Check if the boxes can be moved
                if (canMoveBoxes(newPosition, move, newGrid)) {
                    moveBoxes(newPosition, move, newGrid)
                    newGrid[newPosition] = Icon.ROBOT
                    newGrid[robot] = Icon.OPEN
                    robot = newPosition
                }
            }
        }
        return Warehouse(newGrid, robot, moves)
    }

    private fun canMoveBoxes(
        newPosition: Point,
        direction: Direction,
        grid: List<List<Icon>>,
    ): Boolean {
        if (grid.getOrDefault(newPosition, Icon.WALL) == Icon.WALL) return false
        if (grid[newPosition] == Icon.OPEN) return true
        if (grid[newPosition] == Icon.BOX) {
            val nextPosition = newPosition.move(direction)
            return canMoveBoxes(nextPosition, direction, grid)
        }
        return false
    }

    private fun moveBoxes(
        newPosition: Point,
        direction: Direction,
        grid: List<MutableList<Icon>>,
    ) {
        if (grid[newPosition] == Icon.BOX) {
            val nextPosition = newPosition.move(direction)
            if (grid.getOrDefault(newPosition, Icon.WALL) == Icon.WALL) return
            moveBoxes(nextPosition, direction, grid)
            grid[newPosition] = Icon.OPEN
            grid[nextPosition] = Icon.BOX
        }
    }
}

enum class Icon(
    val value: Char,
) {
    WALL('#'),
    OPEN('.'),
    BOX('O'),
    BOX_L('['),
    BOX_R(']'),
    ROBOT('@'),
    ;

    val isBox: Boolean
        get() = this == BOX_L || this == BOX_R

    companion object {
        fun fromChar(char: Char): Icon = entries.firstOrNull { it.value == char } ?: throw IllegalArgumentException("Unknown Icon: $char")
    }
}
