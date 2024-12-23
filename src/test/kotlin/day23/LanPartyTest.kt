package day23

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 23 - LAN Party")
@TestMethodOrder(OrderAnnotation::class)
class LanPartyTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 7`() {
        assertEquals(7, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 'co,de,ka,ta'`() {
        assertEquals("co,de,ka,ta", sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 1485`() {
        assertEquals(1485, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 'cc,dz,ea,hj,if,it,kf,qo,sk,ug,ut,uv,wh'`() {
        assertEquals("cc,dz,ea,hj,if,it,kf,qo,sk,ug,ut,uv,wh", solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val edges = data.map { it.split("-") }.map { it[0] to it[1] }
    private val graph: Map<String, Set<String>>

    init {
        val g = mutableMapOf<String, MutableSet<String>>()
        edges.forEach { (a, b) ->
            g.getOrPut(a) { mutableSetOf() }.add(b)
            g.getOrPut(b) { mutableSetOf() }.add(a)
        }
        graph = g
    }

    fun solvePartOne(): Int {
        var count = 0

        for (c1 in graph.keys) {
            for (c2 in graph[c1] ?: emptySet()) {
                for (c3 in (graph[c1] ?: emptySet()).intersect((graph[c2] ?: emptySet()).toSet())) {
                    if (c1 >= c2) continue
                    if (c2 >= c3) continue

                    if (listOf(c1, c2, c3).any { it.startsWith('t') }) {
                        count++
                    }
                }
            }
        }

        return count
    }

    fun solvePartTwo(): String {
        val cliques = mutableListOf<Set<String>>()
        bronKerbosch(emptySet(), graph.keys.toMutableSet(), mutableSetOf(), cliques)
        val largestClique =
            cliques
                .maxByOrNull { it.size }
                .orEmpty()
        return largestClique.sorted().joinToString(",")
    }

    /**
     * This is an implementation of the [Bron-Kerbosch algorithm (with pivoting)](https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm#With_pivoting)
     * to find all maximal cliques in a graph.
     */
    private fun bronKerbosch(
        r: Set<String>,
        p: MutableSet<String>,
        x: MutableSet<String>,
        cliques: MutableList<Set<String>>,
    ) {
        if (p.isEmpty() && x.isEmpty()) {
            cliques.add(r.toSet())
            return
        }

        val pivot = (p + x).first()

        for (v in p - graph[pivot].orEmpty()) {
            bronKerbosch(
                r + v,
                p.intersect(graph[v].orEmpty()).toMutableSet(),
                x.intersect(graph[v].orEmpty()).toMutableSet(),
                cliques,
            )
            p -= v
            x += v
        }
    }
}
