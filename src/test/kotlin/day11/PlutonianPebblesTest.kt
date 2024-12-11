package day11

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 11 - Plutonian Pebbles")
@TestMethodOrder(OrderAnnotation::class)
class PlutonianPebblesTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 55312`() {
        assertEquals(55312, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 65601038650482`() {
        assertEquals(65601038650482, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 194782`() {
        assertEquals(194782, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 233007586663131`() {
        assertEquals(233007586663131, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val pebbles = Pebbles(data[0].split("\\s+".toRegex()).map { Pebble(it.trim().toLong()) })

    fun solvePartOne(): Int = pebbles.naiveSplit(25)

    fun solvePartTwo(): Long = pebbles.split(75)
}

data class Pebbles(
    val pebbles: List<Pebble>,
) {
    /**
     * This is the original implementation that I had for part 1, it simply maintains an ever-growing list of pebbles
     * as each iteration splits the individual pebbles. It worked fine for sample and real input for part 1,
     * but it was too slow and blew up the heap space for part 2 (both sample and real input).
     */
    fun naiveSplit(times: Int): Int {
        var newPebbles = pebbles.toList()
        repeat(times) {
            newPebbles = newPebbles.flatMap { it.split() }
        }
        return newPebbles.size
    }

    /**
     * This is the optimized implementation that I came up with for part 2. Instead of keeping track of every individual pebble,
     * I keep track of the count of each pebble number, since as you iterate and split pebbles, the same pebble number
     * can end up occurring many times. This is shown even in the small sample input where several numbers show up
     * several times even in the 6 iterations in the example.
     */
    fun split(times: Int): Long {
        var pebbleCounts = pebbles.groupingBy { it.number }.eachCount().mapValues { it.value.toLong() }
        repeat(times) {
            val newPebbleCounts = mutableMapOf<Long, Long>()
            for ((number, count) in pebbleCounts) {
                val splitPebbles = Pebble(number).split()
                for (pebble in splitPebbles) {
                    newPebbleCounts[pebble.number] = newPebbleCounts.getOrDefault(pebble.number, 0L) + count
                }
            }
            pebbleCounts = newPebbleCounts
        }
        return pebbleCounts.entries.sumOf { it.value }
    }
}

data class Pebble(
    val number: Long,
) {
    private val numDigits = number.toString().length

    constructor(number: String) : this(number.toLong())

    fun split(): List<Pebble> =
        when {
            number == 0L -> {
                listOf(Pebble(1))
            }

            numDigits % 2 == 0 -> {
                with(number.toString()) {
                    listOf(
                        Pebble(take(numDigits / 2)),
                        Pebble(takeLast(numDigits / 2)),
                    )
                }
            }

            else -> {
                listOf(Pebble(number * 2024))
            }
        }
}
