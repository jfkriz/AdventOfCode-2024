package day05

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 05 - Print Queue")
@TestMethodOrder(OrderAnnotation::class)
class PrintQueueTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 143`() {
        assertEquals(143, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 123`() {
        assertEquals(123, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 4185`() {
        assertEquals(4185, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 4480`() {
        assertEquals(4480, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val pageRules = data.chunked()[0].map { it.split("|") }.map { it[0].toInt() to it[1].toInt() }.groupBy { it.first }.mapValues { it.value.map { n -> n.second } }
    private val updates = data.chunked()[1].map { it.split(",").map { n -> n.toInt()} }

    fun solvePartOne(): Int {
        val orderedUpdates = updates.filter { isUpdateInProperOrder(it) }

        return orderedUpdates.sumOf { it[it.size / 2] }
    }

    private fun isUpdateInProperOrder(update: List<Int>): Boolean {
        val applicableRules = pageRules.filter { update.contains(it.key) }

        return update.mapIndexed { index, u ->
            // If there is a rule, make sure the numbers in the update follow the order of the rule
            applicableRules[u]?.all { r ->
                !update.contains(r) || (update.contains(r) && update.indexOf(r) > index)
            } ?: true
        }.all { it }
    }

    fun solvePartTwo(): Int {
        val unorderedUpdates = updates.filter { !isUpdateInProperOrder(it) }

        val ordered = unorderedUpdates.map { sortUpdates(it) }

        return ordered.sumOf { it[it.size / 2] }
    }

    private fun sortUpdates(numbers: List<Int>): List<Int> {
        // Step 1: Build the graph
        val graph = mutableMapOf<Int, MutableList<Int>>()
        val inDegree = mutableMapOf<Int, Int>()

        // Initialize graph and in-degree
        val uniqueNumbers = numbers.toSet() // Ensure we only care about numbers in the input list
        uniqueNumbers.forEach { num ->
            graph[num] = mutableListOf()
            inDegree[num] = 0
        }

        // Populate graph and in-degree based on rules
        for ((key, values) in pageRules) {
            if (key !in uniqueNumbers) continue // Ignore keys not in the input numbers
            for (value in values) {
                if (value in uniqueNumbers) { // Only add edges for relevant values
                    graph[key]?.add(value)
                    inDegree[value] = inDegree.getOrDefault(value, 0) + 1
                }
            }
        }

        // Step 2: Perform topological sort
        val sortedList = mutableListOf<Int>()
        val queue = ArrayDeque<Int>()

        // Add all nodes with in-degree 0 to the queue
        inDegree.forEach { (num, degree) ->
            if (degree == 0) queue.add(num)
        }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            sortedList.add(current)

            // Reduce the in-degree of neighbors and add to queue if in-degree becomes 0
            graph[current]?.forEach { neighbor ->
                inDegree[neighbor] = inDegree[neighbor]!! - 1
                if (inDegree[neighbor] == 0) {
                    queue.add(neighbor)
                }
            }
        }

        // Check if topological sort is possible (i.e., no cycles among relevant nodes)
        return if (sortedList.size >= uniqueNumbers.size) {
            // Return sorted list filtered to include only the original input numbers, in order
            sortedList.filter { it in uniqueNumbers }
        } else {
            emptyList() // Return null if there is a cycle
        }
    }
}
