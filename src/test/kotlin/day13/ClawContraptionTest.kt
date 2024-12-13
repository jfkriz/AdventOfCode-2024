package day13

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 13 - Claw Contraption")
@TestMethodOrder(OrderAnnotation::class)
class ClawContraptionTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 480`() {
        assertEquals(480, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 875318608908`() {
        assertEquals(875318608908, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 36838`() {
        assertEquals(36838, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 83029436920891`() {
        assertEquals(83029436920891, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val clawMachines = data.chunked().map { ClawMachine.fromInputLines(it) }

    fun solvePartOne(): Long = clawMachines.mapNotNull { it.findCost() }.sum()

    fun solvePartTwo(): Long = clawMachines.mapNotNull { it.findCost(true) }.sum()
}

data class ClawMachine(
    val buttonA: Pair<Int, Int>,
    val buttonB: Pair<Int, Int>,
    val targetPoint: Pair<Int, Int>,
) {
    fun findCost(part2: Boolean = false): Long? {
        val aCost = 3
        val bCost = 1
        val targetIncrease = if (part2) 10000000000000 else 0L
        val maxPresses = if (!part2) 100 else null

        val presses =
            (if (part2) findButtonPressesDeterminants(targetIncrease) else findButtonPressesOriginal()) ?: return null
        if (maxPresses != null && (presses.first > maxPresses || presses.second > maxPresses)) return null

        return (presses.first * aCost) + (presses.second * bCost)
    }

    /**
     * This is the original implementation that uses a brute force approach to find the button presses. This is solving
     * the system of equations by iterating through all possible values of a and b. This is not efficient for part 2...
     */
    private fun findButtonPressesOriginal(targetIncrease: Long = 0): Pair<Long, Long>? {
        val (aX, aY) = buttonA.first.toLong() to buttonA.second.toLong()
        val (bX, bY) = buttonB.first.toLong() to buttonB.second.toLong()
        val (tX, tY) = targetPoint.first + targetIncrease to targetPoint.second + targetIncrease

        // This is really slow with a large targetIncrease value
        // We can optimize it by using the determinants of the system of equations
        // That is done by the findButtonPressesDeterminants function
        for (a in 0..tX / aX) {
            val remainingX = tX - a * aX
            if (remainingX % bX == 0L) {
                val b = remainingX / bX
                if (b >= 0 && a * aY + b * bY == tY) {
                    return Pair(a, b)
                }
            }
        }
        return null
    }

    /**
     * This is the optimized implementation that uses the determinants of the system of equations to find the button. This
     * was mostly generated by pairing with my AI Copilot :)
     */
    private fun findButtonPressesDeterminants(targetIncrease: Long = 0): Pair<Long, Long>? {
        val (aX, aY) = buttonA.first.toLong() to buttonA.second.toLong()
        val (bX, bY) = buttonB.first.toLong() to buttonB.second.toLong()
        val (tX, tY) = targetPoint.first + targetIncrease to targetPoint.second + targetIncrease

        val d = aX * bY - aY * bX
        val da = bY * tX - bX * tY
        val db = aX * tY - aY * tX

        when {
            d == 0L -> return null
            da % d != 0L || db % d != 0L -> return null
            else -> return Pair(da / d, db / d)
        }
    }

    companion object {
        private val xyRegex = "X[+=](\\d+),\\sY[+=](\\d+)".toRegex()

        fun fromInputLines(lines: List<String>): ClawMachine {
            require(lines.size == 3) { "Invalid input" }
            val buttonA =
                xyRegex.find(lines[0])?.let {
                    val (x, y) = it.destructured
                    Pair(x.toInt(), y.toInt())
                } ?: throw IllegalArgumentException("Invalid input")
            val buttonB =
                xyRegex.find(lines[1])?.let {
                    val (x, y) = it.destructured
                    Pair(x.toInt(), y.toInt())
                } ?: throw IllegalArgumentException("Invalid input")
            val targetPoint =
                xyRegex.find(lines[2])?.let {
                    val (x, y) = it.destructured
                    Pair(x.toInt(), y.toInt())
                } ?: throw IllegalArgumentException("Invalid input")
            return ClawMachine(buttonA, buttonB, targetPoint)
        }
    }
}
