package day04

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction

@DisplayName("Day 04 - Ceres Search")
@TestMethodOrder(OrderAnnotation::class)
class CeresSearchTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 18`() {
        assertEquals(18, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 9`() {
        assertEquals(9, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 2591`() {
        assertEquals(2591, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1880`() {
        assertEquals(1880, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val matrix = data.map { it.toList() }
    private val rows = matrix.size
    private val cols = matrix[0].size

    fun solvePartOne(): Int {
        var count = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                for (direction in Direction.entries) {
                    if (findXmas(row, col, direction)) {
                        count++
                    }
                }
            }
        }
        return count
    }

    private fun findXmas(
        x: Int,
        y: Int,
        direction: Direction,
    ): Boolean {
        val word = "XMAS"
        for (i in word.indices) {
            val newX = x + direction.xOffset * i
            val newY = y + direction.yOffset * i
            if (newX !in 0 until rows || newY !in 0 until cols || matrix[newX][newY] != word[i]) {
                return false
            }
        }
        return true
    }

    fun solvePartTwo(): Int {
        var count = 0
        for (row in 1 until rows - 1) {
            for (col in 1 until cols - 1) {
                if (findX(row, col)) {
                    count++
                }
            }
        }
        return count
    }

    private fun findX(
        x: Int,
        y: Int,
    ): Boolean {
        // If the center is not an A, then it's not a MAS
        if (matrix[x][y] != 'A') {
            return false
        }

        val topLeft = matrix[x - 1][y - 1]
        val topRight = matrix[x - 1][y + 1]
        val bottomLeft = matrix[x + 1][y - 1]
        val bottomRight = matrix[x + 1][y + 1]

        // MAS from top to bottom
        if (topLeft == 'M' && topRight == 'M' && bottomLeft == 'S' && bottomRight == 'S') {
            return true
        }

        // MAS from bottom to top
        if (topLeft == 'S' && topRight == 'S' && bottomLeft == 'M' && bottomRight == 'M') {
            return true
        }

        // MAS from left to right
        if (topLeft == 'M' && topRight == 'S' && bottomLeft == 'M' && bottomRight == 'S') {
            return true
        }

        // MAS from right to left
        if (topLeft == 'S' && topRight == 'M' && bottomLeft == 'S' && bottomRight == 'M') {
            return true
        }

        return false
    }
}
