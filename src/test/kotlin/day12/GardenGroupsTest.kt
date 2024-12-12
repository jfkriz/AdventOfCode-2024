package day12

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.Point
import util.extensions.contains
import util.extensions.get

@DisplayName("Day 12 - Garden Groups")
@TestMethodOrder(OrderAnnotation::class)
class GardenGroupsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 1930`() {
        assertEquals(1930, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 1206`() {
        assertEquals(1206, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 1437300`() {
        assertEquals(1437300, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 849332`() {
        assertEquals(849332, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    val garden = GardenMap(data.map { it.toList() })

    fun solvePartOne(): Int = garden.totalCost

    fun solvePartTwo(): Int = garden.totalCostWithBulkDiscount
}

data class GardenMap(
    val grid: List<List<Char>>,
) {
    val regions: List<Region> by lazy {
        // Regions in a grid are defined as contiguous groups of a character. Any group of a single character that touches
        // horizontally or vertically is considered a region. The same character can be in multiple regions that are not touching.
        // Also, regions can appear within other regions.
        val regions = mutableListOf<List<Point>>()
        val visited = mutableSetOf<Point>()
        grid.forEachIndexed { y, row ->
            row.forEachIndexed { x, value ->
                val point = Point(x, y)
                if (point !in visited) {
                    val region = mutableListOf<Point>()
                    regions.add(region)
                    findRegion(point, value, region, visited)
                }
            }
        }

        regions.map { Region(grid[it[0]], it) }
    }

    val totalCost: Int by lazy {
        regions.sumOf { it.price }
    }

    val totalCostWithBulkDiscount: Int by lazy {
        regions.sumOf { it.priceWithBulkDiscount }
    }

    private fun findRegion(
        point: Point,
        value: Char,
        region: MutableList<Point>,
        visited: MutableSet<Point>,
    ) {
        if (point !in grid || point in visited || grid[point] != value) {
            return
        }

        visited.add(point)
        region.add(point)

        Direction.entries.filterNot { it.diagonal }.forEach { direction ->
            findRegion(point.move(direction), value, region, visited)
        }
    }
}

data class Region(
    val identifier: Char,
    val points: List<Point>,
) {
    val price: Int by lazy {
        area * perimeter
    }

    val priceWithBulkDiscount: Int by lazy {
        area * sides
    }

    private val area: Int = points.size

    private val perimeter: Int by lazy {
        findPerimeterPoints().size
    }

    private fun findPerimeterPoints(): List<Point> {
        val perimeterPoints = mutableListOf<Point>()
        points.forEach { point ->
            Direction.entries.filterNot { it.diagonal }.forEach { direction ->
                val neighbor = point.move(direction)
                if (neighbor !in points) {
                    perimeterPoints.add(point)
                }
            }
        }
        return perimeterPoints
    }

    private val sides: Int by lazy {
        var total = 0

        for (point in points) {
            // Outer corners - look for neighboring points not in the region
            val (up, down, right, left) =
                listOf(
                    point.move(Direction.Up),
                    point.move(Direction.Down),
                    point.move(Direction.Right),
                    point.move(Direction.Left),
                )
            total +=
                when (
                    point.neighbors
                        .filterNot { it.key.diagonal }
                        .filterNot { it.value in points }
                        .count()
                ) {
                    4 -> 4 // A single point has 4 corners
                    3 -> 2 // A single point sticking out has 2 outside corners, also maybe 2 inside corners, but we'll handle that later
                    2 -> {
                        // An outside edge has 1 corner - but not if it is in a straight line with another point
                        if ((up in points && down in points) || (right in points && left in points)) 0 else 1
                    }

                    else -> 0 // Anything else means it is either inside the region (not on an edge), or it could be an inner corner
                }

            // Inner corners - look for points within the region that don't have a diagonal neighbor, but do have two neighbors that are in the region
            val innerCorners =
                listOf(
                    Triple(point.move(Direction.UpRight), up, right),
                    Triple(point.move(Direction.DownRight), down, right),
                    Triple(point.move(Direction.UpLeft), up, left),
                    Triple(point.move(Direction.DownLeft), down, left),
                )

            innerCorners
                .count { (corner, side1, side2) ->
                    corner !in points && side1 in points && side2 in points
                }.let { total += it }
        }
        total
    }
}
