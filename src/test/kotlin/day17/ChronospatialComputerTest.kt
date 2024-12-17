package day17

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.pow

@DisplayName("Day 17 - Chronospatial Computer")
@TestMethodOrder(OrderAnnotation::class)
class ChronospatialComputerTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }
    private val samplePart2Solver by lazy {
        Solver(loadOtherInput("test-input-part-2.txt"))
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 4,6,3,5,6,3,5,2,1,0`() {
        assertEquals("4,6,3,5,6,3,5,2,1,0", sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 117440`() {
        assertEquals(117440, samplePart2Solver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 5,0,3,5,7,6,1,5,4`() {
        assertEquals("5,0,3,5,7,6,1,5,4", solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 164516454365621`() {
        assertEquals(164516454365621L, solver.solvePartTwo())
    }

    @Test
    @Disabled("This was used to try to find the pattern when running instructions for register A value from 1 to 8^7.")
    fun showComputers() {
        // Leaving this here for posterity - I used this when I was trying to track down the pattern for part 2.
        // This shows the output of processing the computer instructions for each value of register A from 1 to 8^7.
        val computer = Computer.fromInput(loadInput())
        for (i in 1.toLong()..8.0.pow(7).toLong()) {
            val result = Computer(i, 0, 0, computer.instructions).processInstructions()
            println("$i: $result")
        }
    }
}

class Solver(
    data: List<String>,
) {
    private val computer = Computer.fromInput(data)

    fun solvePartOne(): String = computer.processInstructions().joinToString(",")

    // My initial part 2 solution was looking for the digits in the program L->R, but that failed to find the program past the first 4 digits.
    // Saw this post in the subreddit: https://www.reddit.com/r/adventofcode/comments/1hg69ql/2024_day_17_part_2_can_someone_please_provide_a/
    // And realized I was on the right track, but I needed to look for the program starting with the last digit, then the last 2, then the last
    // 3, etc. until I found the register A value that gave the whole thing.
    fun solvePartTwo(): Long {
        var computer = Computer(0, 0, 0, computer.instructions)
        val program =
            this.computer.instructions
                .map { listOf(it.operation.num.toLong(), it.operand) }
                .flatten()

        var iterations = 0L
        var registerA = 0L
        for (i in program.indices.reversed()) {
            val partial = program.subList(i, program.size)
            registerA *= 8
            computer = Computer(registerA, 0, 0, computer.instructions)
            var result = computer.processInstructions()
            while (result != partial) {
                iterations++
                computer = Computer(++registerA, 0, 0, computer.instructions)
                result = computer.processInstructions()
            }
        }
        println("Iterations: $iterations")
        return registerA
    }
}

data class Computer(
    val registerA: Long,
    val registerB: Long,
    val registerC: Long,
    val instructions: List<Instruction>,
    val instructionPointer: Int = 0,
    val output: List<Long> = emptyList(),
) {
    fun processInstructions(): List<Long> {
        var currentComputer = this
        while (currentComputer.instructionPointer >= 0 && currentComputer.instructionPointer < currentComputer.instructions.size) {
            val instruction = currentComputer.instructions[currentComputer.instructionPointer]
            currentComputer = instruction.operation.function(currentComputer, instruction.operand)
        }
        return currentComputer.output
    }

    fun getComboOperand(operand: Long): Double =
        when (operand) {
            in 0L..3L -> operand.toDouble()
            4L -> registerA.toDouble()
            5L -> registerB.toDouble()
            6L -> registerC.toDouble()
            else -> throw IllegalArgumentException("Invalid operand: $operand")
        }

    override fun toString(): String =
        "Computer(registerA=$registerA, registerB=$registerB, registerC=$registerC, output=${
            output.joinToString(
                ",",
            )
        })"

    companion object {
        fun fromInput(input: List<String>): Computer {
            val registerA = input[0].split(':').map { it.trim() }[1].toLong()
            val registerB = input[1].split(':').map { it.trim() }[1].toLong()
            val registerC = input[2].split(':').map { it.trim() }[1].toLong()
            val instructions =
                input[4].split(':')[1].trim().split(',').map { it.toInt() }.windowed(2, 2).map {
                    Instruction(Operation.fromNumber(it[0]), it[1].toLong())
                }
            return Computer(registerA, registerB, registerC, instructions)
        }
    }
}

data class Instruction(
    val operation: Operation,
    val operand: Long,
)

enum class Operation(
    val num: Int,
    val function: (Computer, Long) -> Computer,
) {
    ADV(0, { computer, operand ->
        val numerator = computer.registerA
        val denominator = 2.0.pow(computer.getComboOperand(operand)).toInt()
        computer.copy(registerA = numerator / denominator, instructionPointer = computer.instructionPointer + 1)
    }),
    BXL(1, { computer, operand ->
        computer.copy(
            registerB = computer.registerB.xor(operand),
            instructionPointer = computer.instructionPointer + 1,
        )
    }),
    BST(2, { computer, operand ->
        computer.copy(
            registerB = (computer.getComboOperand(operand) % 8).toLong(),
            instructionPointer = computer.instructionPointer + 1,
        )
    }),
    JNZ(3, { computer, operand ->
        if (computer.registerA == 0L) {
            computer.copy(instructionPointer = computer.instructionPointer + 1)
        } else {
            computer.copy(instructionPointer = (operand / 2).toInt())
        }
    }),
    BXC(4, { computer, _ ->
        computer.copy(
            registerB = computer.registerB.xor(computer.registerC),
            instructionPointer = computer.instructionPointer + 1,
        )
    }),
    OUT(5, { computer, operand ->
        computer.copy(
            output = computer.output + (computer.getComboOperand(operand) % 8).toLong(),
            instructionPointer = computer.instructionPointer + 1,
        )
    }),
    BDV(6, { computer, operand ->
        val numerator = computer.registerA
        val denominator = 2.0.pow(computer.getComboOperand(operand)).toInt()
        computer.copy(registerB = numerator / denominator, instructionPointer = computer.instructionPointer + 1)
    }),
    CDV(7, { computer, operand ->
        val numerator = computer.registerA
        val denominator = 2.0.pow(computer.getComboOperand(operand)).toInt()
        computer.copy(registerC = numerator / denominator, instructionPointer = computer.instructionPointer + 1)
    }),
    ;

    companion object {
        private val values = entries.associateBy(Operation::num)

        fun fromNumber(num: Int): Operation =
            values[num]
                ?: throw IllegalArgumentException("Invalid operation number: $num")
    }
}
