package day07

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 07 - Bridge Repair")
@TestMethodOrder(OrderAnnotation::class)
class BridgeRepairTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 3749`() {
        assertEquals(3749, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 11387`() {
        assertEquals(11387, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 5030892084481`() {
        assertEquals(5030892084481, solver.solvePartOne())
    }

    @Test
    @Order(4)
    @Disabled("This test takes about 5 seconds to run, so skipping it.")
    fun `Part 2 Real Input should return 91377448644679`() {
        assertEquals(91377448644679, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val calibrations = data.map { Calibration(it) }

    fun solvePartOne(): Long {
        return calibrations.filter { it.canNumbersComputeToTestValue(it) }.sumOf { it.testValue }
    }

    fun solvePartTwo(): Long {
        return calibrations.filter { it.canNumbersComputeToTestValue(it, true) }.sumOf { it.testValue }
    }
}

data class Calibration(val testValue: Long, val numbers: List<Int>) {
    constructor(line: String) : this(
        line.split(":")[0].toLong(),
        line.split(":")[1].split(" ").mapNotNull { it.trim().toIntOrNull() },
    )

    /**
     * Determine if the numbers in this calibration can be combined to compute the test value.
     * This is not optimized, it simply creates all possible combinations of operators and checks if any of them
     * can be applied to compute to the test value.
     */
    fun canNumbersComputeToTestValue(
        calibration: Calibration,
        includeConcatenation: Boolean = false,
    ) = generateOperatorCombinations(calibration.numbers.size - 1, includeConcatenation).any { op ->
        var result = calibration.numbers[0].toLong()
        for (i in 1 until calibration.numbers.size) {
            result =
                when (op[i - 1]) {
                    '+' -> result + calibration.numbers[i]
                    '*' -> result * calibration.numbers[i]
                    '|' -> "$result${calibration.numbers[i]}".toLong()
                    else -> throw IllegalArgumentException("Invalid operator")
                }
        }
        result==calibration.testValue
    }

    private fun generateOperatorCombinations(
        n: Int,
        includeConcatenation: Boolean,
    ): List<List<Char>> {
        val result = mutableListOf<String>()

        fun generateCombinations(
            current: String,
            position: Int,
        ) {
            if (position==n) {
                result.add(current)
                return
            }
            // Add '+' to the current combination
            generateCombinations("$current+", position + 1)
            // Add '*' to the current combination
            generateCombinations("$current*", position + 1)
            // If concatenation is allowed, add it to the current combination
            if (includeConcatenation) {
                generateCombinations("$current|", position + 1)
            }
        }

        generateCombinations("", 0)
        return result.map { it.toList() }
    }
}
