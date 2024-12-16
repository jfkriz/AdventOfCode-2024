package day16

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.Point
import util.extensions.getOrDefault
import java.util.PriorityQueue

@DisplayName("Day 16 - Reindeer Maze")
@TestMethodOrder(OrderAnnotation::class)
class ReindeerMazeTest : DataFiles {
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
    fun `Part 1 Sample Input should return 7036`() {
        assertEquals(7036, sampleSolver.solvePartOne())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input 2 should return 11048`() {
        assertEquals(11048, sampleSolver2.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 45`() {
        assertEquals(45, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input 2 should return 64`() {
        assertEquals(64, sampleSolver2.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 74392`() {
        assertEquals(74392, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 426`() {
        assertEquals(426, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val maze = Maze.fromInputLines(data)

    fun solvePartOne(): Int = maze.shortestPath().first

    fun solvePartTwo(): Int = maze.shortestPath().second
}

data class Maze(
    val grid: List<List<Char>>,
    val start: Point,
    val end: Point,
) {
    fun shortestPath(): Pair<Int, Int> {
        val queue = PriorityQueue(listOf(PathState(0, start)))
        val visited = mutableMapOf(Pair(start, Direction.Right) to 0)

        var bestScore = Int.MAX_VALUE
        val bestSeats = mutableSetOf(start)

        while (queue.isNotEmpty()) {
            val state = queue.poll()

            if (state.cost > bestScore) break

            if (state.loc == end) {
                bestScore = state.cost
                bestSeats.addAll(state.visited)
                continue
            }

            if (visited.getOrDefault(Pair(state.loc, state.dir), Int.MAX_VALUE) < state.cost) continue

            visited[Pair(state.loc, state.dir)] = state.cost

            val nextMoves =
                listOf(
                    Pair(state.loc.move(state.dir), state.dir),
                    Pair(state.loc.move(state.dir.rotateClockwise(false)), state.dir.rotateClockwise(false)),
                    Pair(
                        state.loc.move(state.dir.rotateCounterClockwise(false)),
                        state.dir.rotateCounterClockwise(false),
                    ),
                )

            for ((loc, dir) in nextMoves) {
                if (grid.getOrDefault(loc, '.') == '#') continue

                val seen = state.visited + loc

                if (dir == state.dir) {
                    queue.add(PathState(state.cost + 1, loc, dir, seen))
                } else {
                    queue.add(PathState(state.cost + 1001, loc, dir, seen))
                }
            }
        }

        return bestScore to bestSeats.size
    }

    companion object {
        fun fromInputLines(input: List<String>): Maze {
            val grid = input.map { it.toList() }
            val start =
                grid
                    .mapIndexed { y, row ->
                        row.mapIndexedNotNull { x, ch ->
                            if (ch == 'S') {
                                Point(x, y)
                            } else {
                                null
                            }
                        }
                    }.flatten()
                    .firstOrNull() ?: throw IllegalStateException("No start point found")
            val end =
                grid
                    .mapIndexed { y, row ->
                        row.mapIndexedNotNull { x, ch ->
                            if (ch == 'E') {
                                Point(x, y)
                            } else {
                                null
                            }
                        }
                    }.flatten()
                    .firstOrNull() ?: throw IllegalStateException("No end point found")
            return Maze(grid, start, end)
        }
    }
}

data class PathState(
    val cost: Int,
    val loc: Point,
    val dir: Direction = Direction.Right,
    val visited: Set<Point> = emptySet(),
) : Comparable<PathState> {
    override fun compareTo(other: PathState): Int = cost.compareTo(other.cost)
}
