package day24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 24 - Crossed Wires")
@TestMethodOrder(OrderAnnotation::class)
class CrossedWiresTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val sampleSolverPart2 by lazy {
        Solver(loadOtherInput("test-input-part2.txt"))
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 2024`() {
        assertEquals(2024, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    @Disabled("Disabled because the sample input 2 does not implement an adder, so the solution will not find this answer")
    fun `Part 2 Sample Input should return 'z00,z01,z02,z05'`() {
        assertEquals("z00,z01,z02,z05", sampleSolverPart2.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 41324968993486`() {
        assertEquals(41324968993486, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 'bmn,jss,mvb,rds,wss,z08,z18,z23'`() {
        assertEquals("bmn,jss,mvb,rds,wss,z08,z18,z23", solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val wires = data.chunked()[0].map { it.split(": ") }.associate { it[0] to (it[1] == "1") }
    private val instructions = data.chunked()[1].map { Instruction.from(it) }

    fun solvePartOne(): Long {
        val wires = wires.toMutableMap()
        while (instructions.any { !it.evaluate(wires) }) {
            instructions.forEach { it.evaluate(wires) }
        }

        return wires
            .filterKeys { it.startsWith("z") }
            .toList()
            .sortedByDescending { it.first }
            .joinToString("") { if (it.second) "1" else "0" }
            .toLong(2)
    }

    fun solvePartTwo(): String {
        val circuit =
            instructions
                .associateBy { it.outputWire }
                .toMutableMap()
        return fix(circuit).sorted().joinToString(",")
    }

    /**
     * This method assumes that the input circuit implements a full adder for 2 44-bit numbers. It will swap the
     * wires that are not in the correct order and return the list of swapped wires. It will throw an exception if the
     * circuit does not implement a full adder.
     */
    private fun fix(circuit: MutableMap<String, Instruction>): List<String> {
        var cin =
            findOutputWire(circuit, "x00", "AND", "y00")
                ?: throw Exception("Unable to find initial input")

        for (i in 1 until 45) {
            val x = "x%02d".format(i)
            val y = "y%02d".format(i)
            val z = "z%02d".format(i)

            val xor1 = findOutputWire(circuit, x, "XOR", y)
            val and1 = findOutputWire(circuit, x, "AND", y)
            if (xor1 == null || and1 == null) {
                throw IllegalArgumentException("Circuit does not implement a full adder - missing XOR or AND gate for $x and $y")
            }

            val xor2 = findOutputWire(circuit, cin, "XOR", xor1)
            val and2 = findOutputWire(circuit, cin, "AND", xor1)

            if (xor2 == null && and2 == null) {
                return swapAndFix(circuit, xor1, and1)
            }

            if (xor2 != null && xor2 != z) {
                return swapAndFix(circuit, z, xor2)
            } else {
                and2
                    ?: throw IllegalArgumentException("Circuit does not implement a full adder - missing AND gate for $cin and $xor1")
                cin = findOutputWire(circuit, and1, "OR", and2)
                    ?: throw IllegalArgumentException("Circuit does not implement a full adder - missing OR gate for $and1 and $and2")
            }
        }
        return emptyList()
    }

    private fun swapAndFix(
        circuit: MutableMap<String, Instruction>,
        out1: String,
        out2: String,
    ): List<String> {
        val temp = circuit[out1]
        circuit[out1] = circuit[out2]!!
        circuit[out2] = temp!!
        return fix(circuit) + listOf(out1, out2)
    }

    private fun findOutputWire(
        circuit: MutableMap<String, Instruction>,
        x: String,
        operation: String,
        y: String,
    ): String? =
        circuit.entries
            .firstOrNull {
                (it.value.wire1 == x && it.value.operation == operation && it.value.wire2 == y) ||
                    (it.value.wire1 == y && it.value.operation == operation && it.value.wire2 == x)
            }?.key
}

data class Instruction(
    val wire1: String,
    val operation: String,
    val wire2: String,
    val outputWire: String,
) {
    var v1: Boolean? = null
    var v2: Boolean? = null
    var result: Boolean? = null

    fun evaluate(wires: MutableMap<String, Boolean>): Boolean {
        if (result != null) return true

        if (v1 == null && !wires.contains(wire1)) {
            return false
        }
        if (v2 == null && !wires.contains(wire2)) {
            return false
        }

        v1 = v1 ?: wires[wire1]
        v2 = v2 ?: wires[wire2]
        result =
            when (operation) {
                "AND" -> v1!! and v2!!
                "OR" -> v1!! or v2!!
                "XOR" -> v1!! xor v2!!
                else -> throw IllegalArgumentException("Unknown operation: $operation")
            }
        wires[outputWire] = result!!
        return true
    }

    companion object {
        fun from(input: String): Instruction {
            val (wires, outputWire) = input.split(" -> ")
            val (wire1, operation, wire2) = wires.split(" ")
            return Instruction(wire1, operation, wire2, outputWire)
        }
    }
}
