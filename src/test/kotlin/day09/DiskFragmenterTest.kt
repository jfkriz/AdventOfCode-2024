package day09

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 09 - Disk Fragmenter")
@TestMethodOrder(OrderAnnotation::class)
class DiskFragmenterTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 1928`() {
        assertEquals(1928, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 2858`() {
        assertEquals(2858, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 6225730762521`() {
        assertEquals(6225730762521, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 6250605700557`() {
        assertEquals(6250605700557, solver.solvePartTwo())
    }
}

class Solver(
    data: List<String>,
) {
    private val diskMap = DiskMap(data.first())

    fun solvePartOne(): Long = diskMap.checksum

    fun solvePartTwo(): Long = diskMap.defragmentedChecksum
}

data class DiskMap(
    private val entries: List<DiskMapEntry>,
) {
    constructor(data: String) : this(
        data.chunked(2).mapIndexed { index, entry ->
            DiskMapEntry(index, entry)
        },
    )

    private val layout: Array<Int> by lazy {
        entries.flatMap { it.intArray.asList() }.toTypedArray()
    }

    private val compacted: Array<Int> by lazy {
        val diskLayout = layout.clone()

        var firstEmptyIndex = diskLayout.indexOf(-1)
        var lastIndex = diskLayout.lastIndex
        while (lastIndex >= firstEmptyIndex) {
            if (diskLayout[lastIndex] != -1) {
                diskLayout[firstEmptyIndex] = diskLayout[lastIndex]
                diskLayout[lastIndex] = -1
            }
            firstEmptyIndex = diskLayout.indexOf(-1)
            lastIndex = diskLayout.indexOfLast { it != -1 }
        }
        diskLayout
    }

    val checksum: Long by lazy {
        compacted.filterNot { it == -1 }.mapIndexed { position, fileId -> (position * fileId).toLong() }.sum()
    }

    private val defragmented: Array<Int> by lazy {
        val diskLayout =
            layout.fold(mutableListOf<MutableList<Int>>()) { acc, n ->
                if (acc.isEmpty() || acc.last().last() != n) {
                    acc.add(mutableListOf(n))
                } else {
                    acc.last().add(n)
                }
                acc
            }

        for (fileId in diskLayout.reversed().map { it.first() }.filterNot { it == -1 }) {
            val fileLocation = diskLayout.indexOfFirst { it.first() == fileId }
            val file = diskLayout[fileLocation].toMutableList()
            val freeLocation = diskLayout.indexOfFirst { it.first() == -1 && it.size >= file.size }

            if (freeLocation == -1 || fileLocation < freeLocation) {
                continue
            }

            val free = diskLayout[freeLocation].toMutableList()
            if (free.size == file.size) {
                diskLayout[freeLocation] = file
                diskLayout[fileLocation] = free
            } else if (free.size > file.size) {
                val extra = free.drop(file.size).toMutableList()
                val swap = free.take(file.size).toMutableList()
                diskLayout[freeLocation] = file
                diskLayout[fileLocation] = swap
                diskLayout.add(freeLocation + 1, extra)
            }
        }

        diskLayout.flatten().toTypedArray()
    }

    val defragmentedChecksum: Long by lazy {
        defragmented.map { if (it == -1) 0 else it }.mapIndexed { position, fileId -> (position * fileId).toLong() }.sum()
    }
}

data class DiskMapEntry(
    val fileId: Int,
    val blocks: Int,
    val freeSpace: Int,
) {
    constructor(position: Int, entry: String) : this(
        position,
        entry.first().digitToInt(),
        if (entry.length == 2) entry.last().digitToInt() else 0,
    )

    val intArray: Array<Int> by lazy {
        val array = Array(blocks + freeSpace) { -1 }
        for (i in 0 until blocks) {
            array[i] = fileId
        }
        array
    }
}
